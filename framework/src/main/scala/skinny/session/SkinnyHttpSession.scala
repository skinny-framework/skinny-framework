package skinny.session

import javax.servlet.http.HttpServletRequest
import skinny.controller.feature.RequestScopeFeature
import skinny.filter.SkinnySessionFilter
import skinny.logging.LoggerProvider
import skinny.session.jdbc.SkinnySession

/**
 * SkinnySession works as a shared session for multiple servers.
 */
trait SkinnyHttpSession {

  def getAttributeOrElseUpdate(name: String, default: Any): Any

  def getAttribute(name: String): Option[Any]

  def getAs[A](name: String): Option[A] = getAttribute(name).map(_.asInstanceOf[A])

  def setAttribute(name: String, value: Any): Unit

  def removeAttribute(name: String): Unit

  def save(): Unit

  def invalidate(): Unit

}

object SkinnyHttpSession extends LoggerProvider {

  def getOrCreate(request: HttpServletRequest): SkinnyHttpSession = {
    val jsessionIdCookieName: String = request.getServletContext.getSessionCookieConfig.getName
    val jsessionIdInCookie: Option[String] = Option(request.getCookies).flatMap(_.find(_.getName == jsessionIdCookieName).map(_.getValue))
    val jsessionIdInSession: Option[String] = Option(request.getSession).map(_.getId)
    logger.debug(s"[Skinny Session] session id (cookie: ${jsessionIdInCookie}, local session: ${jsessionIdInSession})")

    val expireAt = SkinnySession.getExpireAtFromMaxInactiveInterval(request.getSession.getMaxInactiveInterval)
    val skinnySession = if (jsessionIdInCookie.isDefined && jsessionIdInCookie.get != jsessionIdInSession) {
      SkinnySession.findOrCreate(jsessionIdInCookie.get, jsessionIdInSession, expireAt)
    } else {
      SkinnySession.findOrCreate(jsessionIdInSession.orNull[String], None, expireAt)
    }

    val skinnySessionWrapper = new SkinnyHttpSessionJDBCImpl(request.getSession, skinnySession)
    logger.debug("[Skinny Session] " +
      s"initial attributes: ${skinnySession.attributeNames.map(name => s"$name -> ${skinnySession.getAttribute(name)}")}")
    val requestScope = RequestScopeFeature.requestScope(request)
    requestScope += (SkinnySessionFilter.ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE -> skinnySessionWrapper)
    skinnySessionWrapper
  }

}
