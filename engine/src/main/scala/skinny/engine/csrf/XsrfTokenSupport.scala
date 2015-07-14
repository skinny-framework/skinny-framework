package skinny.engine.csrf

import javax.servlet.http.HttpServletRequest

import skinny.engine.{ RouteTransformer, SkinnyEngineBase }

object XsrfTokenSupport {

  val DefaultKey = "skinny.engine.XsrfTokenSupport.key"

  val HeaderNames = Vector("X-XSRF-TOKEN")

  val CookieKey = "XSRF-TOKEN"

}

trait XsrfTokenSupport { this: SkinnyEngineBase =>

  import XsrfTokenSupport._

  /**
   * The key used to store the token on the session, as well as the parameter
   * of the request.
   */
  def xsrfKey: String = DefaultKey

  /**
   * Returns the token from the session.
   */
  def xsrfToken(implicit request: HttpServletRequest): String =
    request.getSession.getAttribute(xsrfKey).asInstanceOf[String]

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
    !mainThreadRequest.requestMethod.isSafe &&
      session.get(xsrfKey) != params.get(xsrfKey) &&
      !HeaderNames.map(mainThreadRequest.headers.get).contains(session.get(xsrfKey))

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
    session.getOrElseUpdate(xsrfKey, CsrfTokenGenerator.apply())
    val cookieOpt = cookies.get(CookieKey)
    if (cookieOpt.isEmpty || cookieOpt != session.get(xsrfKey)) {
      cookies += CookieKey -> xsrfToken
    }
  }
}