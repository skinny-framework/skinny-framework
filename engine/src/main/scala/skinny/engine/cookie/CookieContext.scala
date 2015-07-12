package skinny.engine.cookie

import javax.servlet.http.HttpServletRequest

import skinny.engine.context.SkinnyEngineContext
import skinny.engine.cookie.CookieSupport._

trait CookieContext { self: SkinnyEngineContext =>

  implicit def cookieOptions: CookieOptions = {
    servletContext.get(CookieOptionsKey).orNull.asInstanceOf[CookieOptions]
  }

  def cookies(implicit request: HttpServletRequest): SweetCookies = {
    request.get(SweetCookiesKey).orNull.asInstanceOf[SweetCookies]
  }

}
