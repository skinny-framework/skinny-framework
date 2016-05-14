package skinny.test

import java.io.File
import java.net.URI

import com.typesafe.config.{ Config, ConfigFactory }
import skinny.logging.LoggerProvider

import scala.collection.JavaConverters._
import scalikejdbc._
import skinny.orm.feature.CRUDFeatureWithId
import skinny.exception.FactoryGirlException
import skinny.orm.ParameterBinderOps
import skinny.util.{ DateTimeUtil, JavaReflectAPI }

import scala.collection.concurrent.TrieMap
import scala.util.Try

/**
 * Test data generator highly inspired by thoughtbot/factory_girl
 *
 * @see "https://github.com/thoughtbot/factory_girl"
 */
case class FactoryGirl[Id, Entity](mapper: CRUDFeatureWithId[Id, Entity], name: Symbol = null) extends LoggerProvider {

  private[this] val c = mapper.column

  val autoSession = AutoSession

  private[this] val valuesToReplaceVariablesInConfig = new scala.collection.concurrent.TrieMap[Symbol, Any]()
  private[this] val additionalNamedValues = new scala.collection.concurrent.TrieMap[Symbol, Any]()

  private[this] val cachedFactoryConfig: TrieMap[String, Config] = new TrieMap[String, Config]()
  private[this] lazy val resolvedConfigs: Seq[Config] = {
    val confDir = "factories"

    getClass.getClassLoader
      .getResources(confDir)
      .asScala.flatMap { url =>

        // url.getFile() may contain %-escaped characters if the path contains spaces
        // for example. It's a reported issue at
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4466485 and won't be fixed.
        // The following line is a suggested workaround in the above url.
        val path = new URI(url.toString).getPath

        new File(path).listFiles
          .withFilter { f => f.isFile && f.getName.endsWith(".conf") }
          .map { f => s"${confDir}/${f.getName}" }
      }.map(ConfigFactory.parseResources(getClass.getClassLoader, _).resolve()).toSeq
  }

  /**
   * Set named values to bind #{name} in "src/test/resources/factories.conf".
   *
   * @param namedValues named values
   * @return self
   */
  def withVariables(namedValues: (Symbol, Any)*): FactoryGirl[Id, Entity] = {
    namedValues.foreach { case (key, value) => valuesToReplaceVariablesInConfig.put(key, value) }
    this
  }

  /**
   * Returns the prefix of factory settings.
   *
   * @return prefix
   */
  def factoryName: Symbol = {
    val n = Option(name).map(_.name).getOrElse(JavaReflectAPI.classSimpleName(mapper))
    Symbol((n.head.toLower + n.tail).replaceFirst("\\$$", ""))
  }

  /**
   * Loads attributes from "src/test/resources/factories.conf".
   *
   * @return attributes in conf file
   */
  def loadedAttributes(): Map[SQLSyntax, Any] = {
    val rootResolvedConfig = ConfigFactory.parseResources(getClass.getClassLoader, "factories.conf").resolve()

    val config = cachedFactoryConfig.getOrElseUpdate(factoryName.name, {
      (rootResolvedConfig +: resolvedConfigs)
        .collectFirst({
          case c if Try { c.getConfig(factoryName.name) }.isSuccess => c.getConfig(factoryName.name)
        }).getOrElse(rootResolvedConfig.getConfig(factoryName.name))
    })

    config.root().unwrapped().asScala.map { case (k, v) => c.field(k) -> v.toString }.toMap
  }

  /**
   * Appends additional named values.
   * @param attributes attributes
   * @return self
   */
  def withAttributes(attributes: (Symbol, Any)*): FactoryGirl[Id, Entity] = {
    attributes.foreach { case (key, value) => additionalNamedValues.put(key, value) }
    this
  }

  private def toTypedValue(value: Any): Any = value match {
    case str: String if Try(str.toBoolean).isSuccess => str.toBoolean
    case str: String if Try(str.toLong).isSuccess => str.toLong
    case str: String if Try(str.toDouble).isSuccess => str.toDouble
    case str: String if DateTimeUtil.isDateTimeFormat(str) => DateTimeUtil.parseDateTime(str)
    case str: String if DateTimeUtil.isLocalDateFormat(str) => DateTimeUtil.parseLocalDate(str)
    case value => value
  }

  private def eval(v: String): Any = {
    if (v == null) null
    else if (v.matches(".*\\$\\{.+\\}.*")) {
      import scala.reflect.runtime.currentMirror
      import scala.tools.reflect.ToolBox
      val toolbox = currentMirror.mkToolBox()
      val tree = toolbox.parse("s\"\"\"" + v + "\"\"\"")
      toTypedValue(toolbox.eval(tree))
    } else toTypedValue(v)
  }

  /**
   * Creates a record with factories.conf & some replaced attributes.
   *
   * @param attributes attributes
   * @param s session
   * @return created entity
   */
  def create(attributes: (Symbol, Any)*)(implicit s: DBSession = autoSession): Entity = {

    val mergedAttributes: Seq[(SQLSyntax, Any)] = (additionalNamedValues ++ attributes).foldLeft(loadedAttributes()) {
      case (xs, (Symbol(key), value)) =>
        if (xs.exists(_._1 == mapper.column.field(key))) {
          xs.map {
            case (k, _) if k == mapper.column.field(key) => (k, value)
            case (k, v) => (k, v)
          }
        } else xs.updated(c.field(key), value)

    }.map(ParameterBinderOps.extractValueFromParameterBinder(_)).map {
      case (key, value) => {
        if (value.toString.contains("#{")) {
          val replacements: Seq[(String, String)] = "#\\{[^\\}]+\\}".r.findAllIn(value.toString).map { matched =>
            val variableKey = matched.trim.replaceAll("[#{}]", "")
            val replacedValue: String = valuesToReplaceVariablesInConfig.get(Symbol(variableKey)).map(_.toString).getOrElse {
              // throw exception when the variable is absent
              throw new IllegalStateException(s"The '#{${variableKey}}' variable used in factories.conf is unset. " +
                s"You should append #withVariables before calling #create() in this case. " +
                s"e.g. FactoryGirl(Entity).withVariables('${variableKey} -> something).create()")
            }
            (matched.replaceFirst("\\{", "\\\\{").replaceFirst("\\}", "\\\\}"), replacedValue)
          }.toSeq
          val replacedValue = replacements.foldLeft(value.toString) {
            case (result, replacement) =>
              result.replaceAll(replacement._1, replacement._2)
          }
          (key, eval(replacedValue))

        } else {
          value match {
            case null => (key, null)
            case None => (key, None)
            case Some(v: String) => (key, eval(v))
            case v: String => (key, eval(v))
            case _ => (key, value)
          }
        }
      }
    }.toSeq

    val id = try mapper.createWithNamedValues(mergedAttributes: _*)
    catch {
      case scala.util.control.NonFatal(e) =>
        val message = s"Failed to create an entity because ${e.getMessage}"
        logger.error(message, e)
        throw new FactoryGirlException(message, e)
    }
    try mapper.findById(id).get
    catch {
      // id might be a raw value because of type erasure
      case e: ClassCastException =>
        try mapper.findById(mapper.rawValueToId(id)).get
        catch {
          case scala.util.control.NonFatal(e) =>
            val message = s"Failed to find created entity because ${e.getMessage}"
            logger.error(message, e)
            throw new FactoryGirlException(message, e)
        }
    }
  }

}

