package skinny.validator

import java.text.MessageFormat
import java.util.Properties
import java.util.Locale
import com.typesafe.config._
import scala.collection.JavaConverters._

object Messages {

  def loadFromConfig(prefix: String = "messages", locale: Option[Locale] = None) = {
    val ext = ".conf"
    val file = locale.map { l => prefix + "_" + l.toString + ext }.getOrElse(prefix + ext)
    val config = ConfigFactory.load(this.getClass.getClassLoader, file)
    val map: Map[String, String] = config.getConfig("error").root().unwrapped().asScala.map { case (k, v) => k -> v.toString }.toMap
    new Messages(map)
  }

  def loadFromProperties(prefix: String = "messages", locale: Option[Locale] = None) = {
    val ext = ".properties"
    val file = locale.map { l => prefix + "_" + l.toString + ext }.getOrElse(prefix + ext)
    val properties = new Properties
    properties.load(this.getClass.getClassLoader.getResourceAsStream(file))
    val map: Map[String, String] = new java.util.HashMap[Any, Any](properties).asScala.filter {
      case (k: String, _) => k.startsWith("error.")
      case _ => false
    }.map {
      case (k, v) => (k.toString.replaceFirst("^error.", "") -> v.toString)
    }.toMap
    new Messages(map)
  }
}

class Messages(map: Map[String, String]) {

  def get(key: String): Option[String] = map.get(key)

  def get(key: String, params: Seq[Any]): Option[String] = {
    get(key).map(template =>
      MessageFormat.format(template, params.map(_.asInstanceOf[Object]): _*))
  }

}

