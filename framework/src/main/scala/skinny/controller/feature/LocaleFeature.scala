package skinny.controller.feature

import java.util.Locale
import javax.servlet.http.HttpServletRequest
import skinny.engine.SkinnyEngineBase

/**
 * Easy-to-use default/session-based Locale configuration.
 */
trait LocaleFeature extends SkinnyEngineBase {

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
  protected def setCurrentLocale(locale: String)(implicit req: HttpServletRequest = request): Unit = {
    session(req).put(sessionLocaleKey, locale)
  }

  /**
   * Returns current locale for this request.
   *
   * @return current locale
   */
  protected def currentLocale(implicit req: HttpServletRequest = request): Option[Locale] = {
    // avoid creating a session
    sessionOption(req)
      .flatMap(_.get(sessionLocaleKey))
      .map(locale => new Locale(locale.toString))
      .orElse(defaultLocale)
  }

}

