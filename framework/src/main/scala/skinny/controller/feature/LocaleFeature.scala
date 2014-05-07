package skinny.controller.feature

import java.util.Locale
import org.scalatra.ScalatraBase

/**
 * Easy-to-use default/session-based Locale configuration.
 */
trait LocaleFeature extends ScalatraBase {

  /**
   * Returns default locale.
   */
  protected def defaultLocale: Option[Locale] = None

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
  protected def setCurrentLocale(locale: String): Unit = session.put(sessionLocaleKey, locale)

  /**
   * Returns current locale for this request.
   *
   * @return current locale
   */
  protected def currentLocale: Option[Locale] = {
    // avoid creating a session
    sessionOption.flatMap(_.get(sessionLocaleKey)).map(l => new Locale(l.toString)).orElse(defaultLocale)
  }

}

