package skinny.engine.context

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse, HttpServletResponseWrapper }

import skinny.engine.ApiFormats
import skinny.engine.cookie.CookieContext
import skinny.engine.implicits.{ ServletApiImplicits, SessionImplicits }
import skinny.engine.request.ReadOnlyHttpServletRequest
import skinny.engine.response.ResponseStatus

object SkinnyEngineContext {

  private class StableValuesContext(
    implicit val request: HttpServletRequest,
    val response: HttpServletResponse,
    val servletContext: ServletContext) extends SkinnyEngineContext
}

import SkinnyEngineContext._

trait SkinnyEngineContext
    extends ServletApiImplicits
    with SessionImplicits
    with CookieContext {

  implicit def request: HttpServletRequest

  implicit def response: HttpServletResponse

  def servletContext: ServletContext

  /**
   * Gets the content type of the current response.
   */
  def contentType: String = response.contentType.orNull[String]

  /**
   * Gets the status code of the current response.
   */
  def status: Int = response.status.code

  /**
   * Sets the content type of the current response.
   */
  def contentType_=(contentType: String): Unit = {
    response.contentType = Option(contentType)
  }

  /**
   * Sets the status code of the current response.
   */
  def status_=(code: Int): Unit = { response.status = ResponseStatus(code) }

  /**
   * Explicitly sets the request-scoped format.  This takes precedence over
   * whatever was inferred from the request.
   */
  def format_=(formatValue: Symbol): Unit = {
    request(ApiFormats.FormatKey) = formatValue.name
  }

  /**
   * Explicitly sets the request-scoped format.  This takes precedence over
   * whatever was inferred from the request.
   */
  def format_=(formatValue: String): Unit = {
    request(ApiFormats.FormatKey) = formatValue
  }

  protected[this] implicit def skinnyEngineContext: SkinnyEngineContext = {
    val reqWrap = new ReadOnlyHttpServletRequest(request)
    val respWrap = new HttpServletResponseWrapper(response)
    new StableValuesContext()(reqWrap, respWrap, servletContext)
  }

}
