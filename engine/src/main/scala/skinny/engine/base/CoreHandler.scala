package skinny.engine.base

import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }

import skinny.engine.cookie.{ SweetCookies, Cookie }
import skinny.engine.implicits.ServletApiImplicits

trait CoreHandler
    extends Handler
    with ServletApiImplicits
    with DynamicScope { self: ServletContextAccessor =>

  /**
   * The default character encoding for requests and responses.
   */
  protected val defaultCharacterEncoding: String = "UTF-8"

  /**
   * Handles a request and renders a response.
   *
   * $ 1. If the request lacks a character encoding, `defaultCharacterEncoding`
   * is set to the request.
   *
   * $ 2. Sets the response's character encoding to `defaultCharacterEncoding`.
   *
   * $ 3. Binds the current `request`, `response`, and `multiParams`, and calls
   * `executeRoutes()`.
   */
  override def handle(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    // As default, the servlet tries to decode params with ISO_8859-1.
    // It causes an EOFException if params are actually encoded with the
    // other code (such as UTF-8)
    if (request.getCharacterEncoding == null) {
      request.setCharacterEncoding(defaultCharacterEncoding)
    }
    request(Cookie.SweetCookiesKey) = new SweetCookies(request, response)
    response.characterEncoding = Some(defaultCharacterEncoding)
    withRequestResponse(request, response) {
      executeRoutes()
    }
  }

  protected def executeRoutes(): Unit

}
