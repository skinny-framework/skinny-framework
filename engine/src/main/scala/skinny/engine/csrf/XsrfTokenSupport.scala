package skinny.engine.csrf

import skinny.engine.base.BeforeAfterDsl
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.{ RouteTransformer, SkinnyEngineBase }

/**
 * Provides cross-site request forgery protection.
 *
 * If a request is determined to be forged, the `handleForgery()` hook is invoked.
 * Otherwise, a token for the next request is prepared with `prepareCsrfToken`.
 */
trait XsrfTokenSupport { this: SkinnyEngineBase with BeforeAfterDsl =>

  import XsrfTokenSupport._

  /**
   * The key used to store the token on the session, as well as the parameter
   * of the request.
   */
  def xsrfKey: String = DefaultKey

  /**
   * Returns the token from the session.
   */
  def xsrfToken(implicit ctx: SkinnyEngineContext): String =
    ctx.request.getSession.getAttribute(xsrfKey).asInstanceOf[String]

  def xsrfGuard(only: RouteTransformer*): Unit = {
    before((only.toSeq ++ Seq[RouteTransformer](isForged)): _*) { handleForgery() }
  }

  before() { prepareXsrfToken() }

  /**
   * Tests whether a request with a unsafe method is a potential cross-site
   * forgery.
   *
   * @return true if the request is an unsafe method (POST, PUT, DELETE, TRACE,
   * CONNECT, PATCH) and the request parameter at `xsrfKey` does not match
   * the session key of the same name.
   */
  protected def isForged: Boolean =
    !request.requestMethod.isSafe &&
      session(context).get(xsrfKey) != params(context).get(xsrfKey) &&
      !HeaderNames.map(request.headers.get).contains(session(context).get(xsrfKey))

  /**
   * Take an action when a forgery is detected. The default action
   * halts further request processing and returns a 403 HTTP status code.
   */
  protected def handleForgery(): Unit = {
    halt(403, "Request tampering detected!")
  }

  /**
   * Prepares a XSRF token.  The default implementation uses `GenerateId`
   * and stores it on the session.
   */
  protected def prepareXsrfToken(): Unit = {
    session(context).getOrElseUpdate(xsrfKey, CsrfTokenGenerator.apply())
    val cookieOpt = cookies(context).get(CookieKey)
    if (cookieOpt.isEmpty || cookieOpt != session(context).get(xsrfKey)) {
      cookies(context) += CookieKey -> xsrfToken(context)
    }
  }
}

object XsrfTokenSupport {

  val DefaultKey = "skinny.engine.XsrfTokenSupport.key"

  val HeaderNames = Vector("X-XSRF-TOKEN")

  val CookieKey = "XSRF-TOKEN"

}
