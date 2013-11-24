package skinny.util

import com.typesafe.config._
import scala.collection.JavaConverters._

object TypesafeConfigReader {

  /**
   * Loads a configuration file.
   *
   * @param resource file resource
   * @return config
   */
  def load(resource: String): Config = ConfigFactory.load(getClass.getClassLoader, resource)

  /**
   * Loads a configuration file as Map object.
   *
   * @param resource file resource
   * @return Map object
   */
  def loadAsMap(resource: String): Map[String, String] = fromConfigToMap(load(resource))

  /**
   * Loads a Map object from Typesafe-config object.
   *
   * @param config config
   * @return Map object
   */
  def fromConfigToMap(config: Config): Map[String, String] = {
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
