package skinny.engine.implicits

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.cookie.CookieSupport._
import skinny.engine.cookie.{ CookieOptions, SweetCookies }

trait CookiesImplicits extends ServletApiImplicits {

  implicit def cookieOptions(implicit ctx: SkinnyEngineContext): CookieOptions = {
    ctx.servletContext.get(CookieOptionsKey).orNull.asInstanceOf[CookieOptions]
  }

  def cookies(implicit ctx: SkinnyEngineContext): SweetCookies = {
    ctx.request.get(SweetCookiesKey).orNull.asInstanceOf[SweetCookies]
  }

}
