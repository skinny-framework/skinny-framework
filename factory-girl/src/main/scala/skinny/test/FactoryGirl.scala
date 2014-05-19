package skinny.test

import com.typesafe.config.ConfigFactory
import com.twitter.util.Eval
import scala.collection.JavaConverters._
import scalikejdbc._
import skinny.orm.feature.CRUDFeatureWithId
import skinny.exception.FactoryGirlException
import skinny.util.JavaReflectAPI
import skinny.logging.Logging

/**
 * Test data generator highly inspired by thoughtbot/factory_girl
 *
 * @see "https://github.com/thoughtbot/factory_girl"
 */
case class FactoryGirl[Id, Entity](mapper: CRUDFeatureWithId[Id, Entity], name: Symbol = null) extends Logging {

  private[this] val c = mapper.column

  val autoSession = AutoSession

  private[this] val valuesToReplaceVariablesInConfig = new scala.collection.concurrent.TrieMap[Symbol, Any]()
  private[this] val additionalNamedValues = new scala.collection.concurrent.TrieMap[Symbol, Any]()

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
    // TODO directory scan and work with factories/*.conf
    val config = ConfigFactory.load(getClass.getClassLoader, "factories.conf").getConfig(factoryName.name)
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

  private def eval(v: String): String = {
    if (v == null) null
    else new Eval(None).apply("s\"\"\"" + v + "\"\"\"")
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
            case (k, _) if k == mapper.column.field(key) => k -> value
            case (k, v) => (k, v)
          }
        } else xs.updated(c.field(key), value)

    }.map {
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
          key -> eval(replacedValue)

        } else {
          value match {
            case null => key -> null
            case None => key -> None
            case Some(v: String) => key -> eval(v)
            case v: String => key -> eval(v)
            case _ => key -> value
          }
        }
      }
    }.toSeq

    val id = try mapper.createWithNamedValues(mergedAttributes: _*)
    catch {
      case e: Exception =>
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
          case e: Exception =>
            val message = s"Failed to find created entity because ${e.getMessage}"
            logger.error(message, e)
            throw new FactoryGirlException(message, e)
        }
    }
  }

}
