package skinny.engine.implicits

import scala.language.implicitConversions

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.cookie.{ Cookie, CookieOptions, SweetCookies }

/**
 * Implicit conversion for Cookie values.
 */
trait CookiesImplicits extends ServletApiImplicits {

  implicit def cookieOptions(implicit ctx: SkinnyEngineContext): CookieOptions = {
    ctx.servletContext.get(Cookie.CookieOptionsKey).orNull.asInstanceOf[CookieOptions]
  }

  def cookies(implicit ctx: SkinnyEngineContext): SweetCookies = {
    ctx.request.get(Cookie.SweetCookiesKey).orNull.asInstanceOf[SweetCookies]
  }

}
