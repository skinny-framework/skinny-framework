package skinny.controller.feature

import java.util.Locale
import skinny.micro.SkinnyMicroBase
import skinny.micro.context.SkinnyContext

/**
 * Easy-to-use default/session-based Locale configuration.
 */
trait LocaleFeature extends SkinnyMicroBase {

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
  protected def setCurrentLocale(locale: String)(implicit ctx: SkinnyContext): Unit = {
    session(ctx).put(sessionLocaleKey, locale)
  }

  /**
   * Returns current locale for this request.
   *
   * @return current locale
   */
  protected def currentLocale(implicit ctx: SkinnyContext): Option[Locale] = {
    // avoid creating a session
    sessionOption(ctx)
      .flatMap(_.get(sessionLocaleKey))
      .map(locale => new Locale(locale.toString))
      .orElse(defaultLocale)
  }

}

