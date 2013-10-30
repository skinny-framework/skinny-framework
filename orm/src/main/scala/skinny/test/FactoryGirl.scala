package skinny.test

import scalikejdbc._, SQLInterpolation._
import skinny.orm.SkinnyCRUDMapper
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import skinny.util.JavaReflectAPI

/**
 * Test data generator highly inspired by thoughtbot/factory_girl
 *
 * @see "https://github.com/thoughtbot/factory_girl"
 */
case class FactoryGirl[Entity](mapper: SkinnyCRUDMapper[Entity], name: Symbol = null) {

  // TODO initialize error handling

  private[this] val c = mapper.column

  val autoSession = AutoSession

  private[this] val registeredValues = new scala.collection.concurrent.TrieMap[Symbol, Any]()

  /**
   * Set named values to bind #{name} in "src/test/resources/factories.conf".
   *
   * @param namedValues named values
   * @return self
   */
  def withValues(namedValues: (Symbol, Any)*): FactoryGirl[Entity] = {
    namedValues foreach {
      case (key, value) =>
        registeredValues.put(key, value)
    }
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
   * Creates a record with factories.conf & some replaced attributes.
   *
   * @param attributes attributes
   * @param s session
   * @return created entity
   */
  def create(attributes: (Symbol, Any)*)(implicit s: DBSession = autoSession): Entity = {
    val mergedAttributes = attributes.foldLeft(loadedAttributes()) {
      case (xs, (Symbol(key), value)) =>
        if (xs.exists(_._1.value == key)) xs.map { case (k, _) => k -> value }
        else xs.updated(c.field(key), value)
    }.map {
      case (key, value) =>
        if (value.toString.startsWith("#")) key -> registeredValues.get(Symbol(value.toString.replaceAll("[#{}]", "")))
        else key -> value
    }.toSeq

    // TODO data type
    val id = mapper.createWithNamedValues(mergedAttributes: _*)
    mapper.findById(id).get
  }

}
