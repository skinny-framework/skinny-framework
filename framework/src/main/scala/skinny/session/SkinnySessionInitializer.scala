package skinny.session

import javax.servlet._
import javax.servlet.http.HttpServletRequest
import org.joda.time.DateTime
import grizzled.slf4j.Logging
import skinny.session.jdbc.SkinnySession
import skinny.session.servlet.{ SkinnyHttpSessionWrapper, SkinnyHttpRequestWrapper }

class SkinnySessionInitializer extends Filter with Logging {

  def init(filterConfig: FilterConfig) = {}
  def destroy() = {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) = {
    val req = request.asInstanceOf[HttpServletRequest]
    val session = req.getSession(true)
    val jsessionIdInSession = session.getId
    val jsessionIdCookieName = req.getServletContext.getSessionCookieConfig.getName
    val jsessionIdInCookie = req.getCookies.find(_.getName == jsessionIdCookieName).map(_.getValue)
    val expireAt = {
      if (session.getMaxInactiveInterval == 0) DateTime.now.plusMonths(6) // 6 months alive is long enough
      else DateTime.now.plusSeconds(session.getMaxInactiveInterval)
    }
    logger.debug(s"[Skinny Session] session id (cookie: ${jsessionIdInCookie}, local session: ${jsessionIdInSession})")
    val skinnySession = if (jsessionIdInCookie.isDefined && jsessionIdInCookie.get != jsessionIdInSession) {
      SkinnySession.findOrCreate(jsessionIdInCookie.get, Option(jsessionIdInSession), expireAt)
    } else {
      SkinnySession.findOrCreate(jsessionIdInSession, None, expireAt)
    }
    chain.doFilter(new SkinnyHttpRequestWrapper(req,
      SkinnyHttpSessionWrapper(session, new SkinnyHttpSessionJDBCImpl(session, skinnySession))), response)
  }

}
