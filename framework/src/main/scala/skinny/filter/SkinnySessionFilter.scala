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

object SkinnySessionFilter {

  val ATTR_SKINNY_SESSION_IN_REQUEST_SCOPE = classOf[SkinnySessionFilter].getCanonicalName + "_SkinnySessionWrapper"

}
/**
 * Enables replacing Servlet session with Skinny's session shared among several Servlet apps.
 *
 * Mounting skinny.session.SkinnySessionInitializer on the top of ScalatraBootstrap.scala is required.
 *
 * {{{
 *   ctx.mount(classOf[SkinnySessionInitializer], "/\*")
 * }}}
 */
trait SkinnySessionFilter extends SkinnyFilter { self: FlashFeature with CSRFProtectionFeature with LocaleFeature =>
  import SkinnySessionFilter._

  def initializeSkinnySession: SkinnyHttpSession = SkinnyHttpSession.getOrCreate(request)

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

  override def currentLocale: Option[Locale] = {
    skinnySession.getAttribute(sessionLocaleKey)
      .map(l => new Locale(l.toString)).orElse(defaultLocale)
  }

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
