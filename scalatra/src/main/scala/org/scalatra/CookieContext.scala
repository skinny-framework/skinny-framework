package org.scalatra

import javax.servlet.http.HttpServletRequest

trait CookieContext { self: ScalatraContext =>

  import org.scalatra.CookieSupport._

  implicit def cookieOptions: CookieOptions = {
    servletContext.get(CookieOptionsKey).orNull.asInstanceOf[CookieOptions]
  }

  def cookies(implicit request: HttpServletRequest): SweetCookies = {
    request.get(SweetCookiesKey).orNull.asInstanceOf[SweetCookies]
  }

}
