package skinny

import java.text.MessageFormat
import java.util.Locale
import skinny.logging.Logging
import skinny.util.TypesafeConfigReader
import scala.collection.concurrent.TrieMap

object I18n {

  private val cachedMessages = new TrieMap[Locale, Map[String, String]]()

}

/**
 * i18n message provider.
 *
 * @param locale locale
 */
case class I18n(locale: Locale = null) extends Logging {

  /**
   * Messages loaded from "src/main/resources/messages_{locale}.conf".
   */
  private[this] lazy val messages: Map[String, String] = I18n.cachedMessages.getOrElseUpdate(locale, {
    logger.debug(s"i18n messages loaded for ${locale}")
    val ext = ".conf"
    val prefix = "messages"
    val resource = Option(locale).map { l => prefix + "_" + l.toString + ext }.getOrElse(prefix + ext)
    TypesafeConfigReader.loadAsMap(resource)
  })

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

}
