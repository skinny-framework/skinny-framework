package skinny.session

import javax.servlet.http.HttpServletRequest
import org.joda.time.DateTime
import skinny.controller.feature.RequestScopeFeature
import skinny.filter.SkinnySessionFilter
import skinny.logging.LoggerProvider

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
    // Only JDBC backend is supported here.
    // See also SkinnySessionFilter#initializeSkinnySession()
    val skinnySessionWrapper = getOrCreateJDBCImpl(request)
    val requestScope = RequestScopeFeature.requestScope(request)
    requestScope += (SkinnySessionFilter.ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE -> skinnySessionWrapper)
    skinnySessionWrapper
  }

  // JDBC backend
  private[this] def getOrCreateJDBCImpl(request: HttpServletRequest): SkinnyHttpSession = {
    val jsessionIdCookieName: String = request.getServletContext.getSessionCookieConfig.getName
    val jsessionIdInCookie: Option[String] = Option(request.getCookies).flatMap(_.find(_.getName == jsessionIdCookieName).map(_.getValue))
    val jsessionIdInSession: Option[String] = Option(request.getSession).map(_.getId)
    logger.debug(s"[Skinny Session] session id (cookie: ${jsessionIdInCookie}, local session: ${jsessionIdInSession})")

    val expireAt: DateTime = jdbc.SkinnySession.getExpireAtFromMaxInactiveInterval(request.getSession.getMaxInactiveInterval)
    val jdbcSession: jdbc.SkinnySession = {
      if (jsessionIdInCookie.isDefined && jsessionIdInCookie.get != jsessionIdInSession) {
        jdbc.SkinnySession.findOrCreate(jsessionIdInCookie.get, jsessionIdInSession, expireAt)
      } else {
        jdbc.SkinnySession.findOrCreate(jsessionIdInSession.orNull[String], None, expireAt)
      }
    }
    val skinnySessionWrapper: SkinnyHttpSession = new SkinnyHttpSessionJDBCImpl(request.getSession, jdbcSession)
    logger.debug("[Skinny Session] " +
      s"initial attributes: ${jdbcSession.attributeNames.map(name => s"$name -> ${jdbcSession.getAttribute(name)}")}")

    skinnySessionWrapper
  }

}
