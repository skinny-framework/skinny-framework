package skinny.engine.i18n

import java.util.Locale

import skinny.engine.base.BeforeAfterDsl
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.{ SkinnyEngineBase, SkinnyEngineException }

/**
 * i18n support.
 */
trait I18nSupport { this: SkinnyEngineBase with BeforeAfterDsl =>

  import I18nSupport._

  before() {
    request(context)(LocaleKey) = resolveLocale(context)
    request(context)(MessagesKey) = provideMessages(locale(context))(context)
  }

  def locale(implicit ctx: SkinnyEngineContext): Locale = {
    if (request(ctx) == null) {
      throw new SkinnyEngineException("There needs to be a request in scope to call locale")
    } else {
      request(ctx).get(LocaleKey).map(_.asInstanceOf[Locale]).orNull
    }
  }

  def userLocales(implicit ctx: SkinnyEngineContext): Array[Locale] = {
    if (request(ctx) == null) {
      throw new SkinnyEngineException("There needs to be a request in scope to call userLocales")
    } else {
      request(ctx).get(UserLocalesKey).map(_.asInstanceOf[Array[Locale]]).orNull
    }
  }

  def messages(key: String)(implicit ctx: SkinnyEngineContext): String = messages(ctx)(key)

  def messages(implicit ctx: SkinnyEngineContext): Messages = {
    if (request(ctx) == null) {
      throw new SkinnyEngineException("There needs to be a request in scope to call messages")
    } else {
      request(ctx).get(MessagesKey).map(_.asInstanceOf[Messages]).orNull
    }
  }

  /**
   * Provides a default Message resolver
   *
   * @param locale Locale used to create instance
   * @return a new instance of Messages, override to provide own implementation
   */
  def provideMessages(locale: Locale)(implicit ctx: SkinnyEngineContext): Messages = Messages(locale)

  /*
  * Resolve Locale based on HTTP request parameter or Cookie
  */
  private def resolveLocale(implicit ctx: SkinnyEngineContext): Locale = resolveHttpLocale(ctx) getOrElse defaultLocale

  /*
   * Get locale either from HTTP param, Cookie or Accept-Language header.
   * 
   * If locale string is found in HTTP param, it will be set
   * in cookie. Later requests will read locale string directly from this
   *
   * If it's not found, then look at Accept-Language header.
   * Locale strings are transformed to [[java.util.Locale]]
   */
  private def resolveHttpLocale(implicit ctx: SkinnyEngineContext): Option[Locale] = {
    (params(ctx).get(LocaleKey) match {
      case Some(localeValue) =>
        cookies(ctx).set(LocaleKey, localeValue)
        Some(localeValue)
      case _ => cookies(ctx).get(LocaleKey)
    }).map(localeFromString(_)(ctx)) orElse resolveHttpLocaleFromUserAgent(ctx)
  }

  /**
   * Accept-Language header looks like "de-DE,de;q=0.8,en-US;q=0.6,en;q=0.4"
   * Specification see [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html]]
   *
   * @return first preferred found locale or None
   */
  private def resolveHttpLocaleFromUserAgent(implicit ctx: SkinnyEngineContext): Option[Locale] = {
    request(ctx).headers.get("Accept-Language") map { s =>
      val locales = s.split(",").map(s => {
        def splitLanguageCountry(s: String): Locale = {
          val langCountry = s.split("-")
          if (langCountry.length > 1) {
            new Locale(langCountry.head, langCountry.last)
          } else {
            new Locale(langCountry.head)
          }
        }
        // If this language has a quality index:
        if (s.indexOf(";") > 0) {
          val qualityLocale = s.split(";")
          splitLanguageCountry(qualityLocale.head)
        } else {
          splitLanguageCountry(s)
        }
      })
      // save all found locales for later user
      request(ctx).setAttribute(UserLocalesKey, locales)
      // We assume that all accept-languages are stored in order of quality
      // (so first language is preferred)
      locales.head
    }
  }

  /**
   * Reads a locale from a String
   * @param in a string like en_GB or de_DE
   */
  private def localeFromString(in: String)(implicit ctx: SkinnyEngineContext): Locale = {
    val token = in.split("_")
    new Locale(token.head, token.last)
  }

  private def defaultLocale: Locale = Locale.getDefault

}

object I18nSupport {

  val LocaleKey: String = "skinny.engine.i18n.locale"

  val UserLocalesKey: String = "skinny.engine.i18n.userLocales"

  val MessagesKey: String = "messages"

}
