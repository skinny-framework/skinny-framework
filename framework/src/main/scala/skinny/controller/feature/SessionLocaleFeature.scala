package skinny.controller.feature

import java.util.Locale

/**
 * Easy-to-use session-based Locale configuration.
 */
trait SessionLocaleFeature extends org.scalatra.ScalatraBase {

  /**
   * Session key to store current locale string.
   * @return key
   */
  def sessionLocaleKey: String = "locale"

  /**
   * Set current locale.
   *
   * @param locale locale string
   */
  def setCurrentLocale(locale: String): Unit = session.put(sessionLocaleKey, locale)

  /**
   * Returns current locale for this request.
   *
   * @return current locale
   */
  def currentLocale: Option[Locale] = session.get(sessionLocaleKey).map(l => new Locale(l.toString))

}

