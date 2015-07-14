package skinny.engine.csrf

object CsrfTokenSupport {

  val DefaultKey = "skinny.engine.CsrfTokenSupport.key"

  val HeaderNames = Vector("X-CSRF-TOKEN")

}

import javax.servlet.http.HttpServletRequest

import CsrfTokenSupport._
import skinny.engine.SkinnyEngineBase

/**
 * Provides cross-site request forgery protection.
 *
 * Adds a before filter.  If a request is determined to be forged, the
 * `handleForgery()` hook is invoked.  Otherwise, a token for the next
 * request is prepared with `prepareCsrfToken`.
 */
trait CsrfTokenSupport { this: SkinnyEngineBase =>

  before(isForged) { handleForgery() }
  before() { prepareCsrfToken() }

  /**
   * Tests whether a request with a unsafe method is a potential cross-site
   * forgery.
   *
   * @return true if the request is an unsafe method (POST, PUT, DELETE, TRACE,
   * CONNECT, PATCH) and the request parameter at `csrfKey` does not match
   * the session key of the same name.
   */
  protected def isForged: Boolean =
    !mainThreadRequest.requestMethod.isSafe &&
      session.get(csrfKey) != params.get(csrfKey) &&
      !CsrfTokenSupport.HeaderNames.map(mainThreadRequest.headers.get).contains(session.get(csrfKey))

  /**
   * Take an action when a forgery is detected. The default action
   * halts further request processing and returns a 403 HTTP status code.
   */
  protected def handleForgery(): Unit = {
    halt(403, "Request tampering detected!")
  }

  /**
   * Prepares a CSRF token.  The default implementation uses `GenerateId`
   * and stores it on the session.
   */
  // NOTE: keep return type as Any for backward compatibility
  protected def prepareCsrfToken(): Any = {
    session.getOrElseUpdate(csrfKey, CsrfTokenGenerator.apply()).toString
  }

  /**
   * The key used to store the token on the session, as well as the parameter
   * of the request.
   */
  def csrfKey: String = CsrfTokenSupport.DefaultKey

  /**
   * Returns the token from the session.
   */
  protected[skinny] def csrfToken(implicit request: HttpServletRequest): String =
    request.getSession.getAttribute(csrfKey).asInstanceOf[String]

}