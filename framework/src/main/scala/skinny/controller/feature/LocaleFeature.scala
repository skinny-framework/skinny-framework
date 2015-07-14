package skinny.controller.feature

import java.util.Locale
import skinny.engine.SkinnyEngineBase
import skinny.engine.context.SkinnyEngineContext

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
  protected def setCurrentLocale(locale: String)(implicit ctx: SkinnyEngineContext): Unit = {
    session(ctx).put(sessionLocaleKey, locale)
  }

  /**
   * Returns current locale for this request.
   *
   * @return current locale
   */
  protected def currentLocale(implicit ctx: SkinnyEngineContext): Option[Locale] = {
    // avoid creating a session
    sessionOption(ctx)
      .flatMap(_.get(sessionLocaleKey))
      .map(locale => new Locale(locale.toString))
      .orElse(defaultLocale)
  }

}

