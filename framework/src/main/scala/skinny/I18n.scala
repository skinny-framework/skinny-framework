package skinny

import java.text.MessageFormat
import java.util.Locale
import com.typesafe.config._
import scala.collection.JavaConverters._

/**
 * i18n message provider.
 *
 * @param locale locale
 */
case class I18n(locale: Locale = null) {

  /**
   * Messages loaded from "src/main/resources/messages_{locale}.conf".
   */
  private[this] val messages: Map[String, String] = {
    val ext = ".conf"
    val prefix = "messages"
    val file = Option(locale).map { l => prefix + "_" + l.toString + ext }.getOrElse(prefix + ext)
    fromConfigToMap(ConfigFactory.load(this.getClass.getClassLoader, file))
  }

  /**
   * Returns i18n value if exists.
   *
   * @param key key
   * @return value if exists
   */
  def get(key: String): Option[String] = messages.get(key)

  /**
   * Returns i18n value if exists.
   *
   * @param key key
   * @param params params
   * @return value if exists
   */
  def get(key: String, params: Seq[Any]): Option[String] = {
    get(key).map(template =>
      MessageFormat.format(template, params.map(_.asInstanceOf[Object]): _*))
  }

  /**
   * Loads a Map object from Typesafe-config object.
   *
   * @param config config
   * @return Map object
   */
  private[this] def fromConfigToMap(config: Config): Map[String, String] = {
    def extract(map: java.util.Map[String, Any]): Map[String, String] = {
      map.asScala.flatMap {
        case (parentKey, value: java.util.Map[_, _]) =>
          extract(value.asInstanceOf[java.util.Map[String, Any]]).map { case (k, v) => s"${parentKey}.${k}" -> v }
        case (key, value) => Map(key -> value)
      }
    }.map { case (k, v) => k -> v.toString }.toMap

    config.root().keySet().asScala.flatMap { parentKey =>
      config.root().unwrapped().get(parentKey) match {
        case map: java.util.Map[_, _] =>
          extract(config.root().unwrapped().asInstanceOf[java.util.Map[String, Any]])
        case value =>
          Map(parentKey -> value)
      }
    }.map { case (k, v) => k -> v.toString }.toMap
  }

}
