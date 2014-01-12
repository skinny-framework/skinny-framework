package skinny.filter

import scala.language.implicitConversions

import skinny.session._
import javax.servlet.http._
import skinny.controller.feature._
import org.scalatra.{ GenerateId, CsrfTokenSupport, FlashMap }
import org.scalatra.FlashMapSupport._
import java.util.Locale
import org.joda.time.DateTime
import skinny.session.jdbc.SkinnySession

/**
 * Enables replacing Servlet session with Skinny's session shared among several Servlet apps.
 *
 * Mounting skinny.session.SkinnySessionInitializer on the top of ScalatraBootstrap.scala is required.
 *
 * {{{
 *   ctx.mount(classOf[SkinnySessionInitializer], "/\*")
 * }}}
 */
trait SkinnySessionFilter extends SkinnyFilter { self: FlashFeature with CSRFProtectionFeature with SessionLocaleFeature =>

  val ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE = classOf[SkinnySessionFilter].getCanonicalName + "_SkinnySessionWrapper"

  def initializeSkinnySession: SkinnyHttpSession = {
    val jsessionIdCookieName = servletContext.getSessionCookieConfig.getName
    val jsessionIdInCookie = request.getCookies.find(_.getName == jsessionIdCookieName).map(_.getValue)
    val expireAt = {
      if (session.getMaxInactiveInterval == 0) DateTime.now.plusMonths(6) // 6 months alive is long enough
      else DateTime.now.plusSeconds(session.getMaxInactiveInterval)
    }
    val jsessionIdInSession = request.getSession.getId
    logger.debug(s"[Skinny Session] session id (cookie: ${jsessionIdInCookie}, local session: ${jsessionIdInSession})")
    val skinnySession = if (jsessionIdInCookie.isDefined && jsessionIdInCookie.get != jsessionIdInSession) {
      SkinnySession.findOrCreate(jsessionIdInCookie.get, Option(jsessionIdInSession), expireAt)
    } else {
      SkinnySession.findOrCreate(jsessionIdInSession, None, expireAt)
    }
    val skinnySessionWrapper = new SkinnyHttpSessionJDBCImpl(request.getSession, skinnySession)
    logger.debug("[Skinny Session] " +
      s"initial attributes: ${skinnySession.attributeNames.map(name => s"$name -> ${skinnySession.getAttribute(name)}")}")
    set(ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE, skinnySessionWrapper)
    skinnySessionWrapper
  }

  beforeAction()(initializeSkinnySession)

  def skinnySession: SkinnyHttpSession = {
    requestScope[SkinnyHttpSession](ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE).getOrElse {
      initializeSkinnySession
    }
  }
  def skinnySession(key: String)(implicit request: HttpServletRequest): Any = skinnySession(key)
  def skinnySession(key: Symbol)(implicit request: HttpServletRequest): Any = skinnySession(key)

  // override FlashMapSupport

  override def flashMapSetSession(f: FlashMap) {
    try {
      skinnySession.setAttribute(SessionKey, f)
    } catch {
      case e: Throwable =>
    }
  }

  // override CsrfTokenSupport

  override protected def isForged: Boolean = {
    if (skinnySession.getAttribute(csrfKey).isEmpty) {
      prepareCsrfToken()
    }
    !request.requestMethod.isSafe &&
      skinnySession.getAttribute(csrfKey) != params.get(csrfKey) &&
      !CsrfTokenSupport.HeaderNames.map(request.headers.get).contains(skinnySession.getAttribute(csrfKey))
  }

  override protected def prepareCsrfToken() = {
    skinnySession.getAttributeOrElseUpdate(csrfKey, GenerateId())
  }

  // override SessionLocaleFeature

  override def setCurrentLocale(locale: String): Unit = skinnySession.setAttribute(sessionLocaleKey, locale)

  override def currentLocale: Option[Locale] = skinnySession.getAttribute(sessionLocaleKey).map(l => new Locale(l.toString))

  afterAction() {
    try {
      requestScope[SkinnyHttpSession](ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE).map { sessionWrapper =>
        sessionWrapper.save()
      }
    } catch {
      case e: Exception =>
        logger.warn(s"Failed to save skinny session because ${e.getMessage}", e)
    }
  }

}
