package skinny.engine.request

import javax.servlet.http.{ HttpServletRequest, HttpServletRequestWrapper }

/**
 * Read-only immutable wrapper for an [[HttpServletRequest]] that can, for the most part, be
 * passed around to different threads.
 *
 * This is necessary because ServletContainers will "recycle" a request once the original HTTP
 * thread is returned, meaning that a lot of attributes are wet to null (in the case of Jetty).
 *
 * Limitations of this class include the following:
 *
 *   - it is mostly immutable (methods on the original request are not given stable values,
 *     nor are methods that return non-primitive types)
 *   - changes made to the original object or this object may not be reflected across threads
 *
 * @param underlying the original HttpServletRequest to wrap
 */
case class StableHttpServletRequest(
  private val underlying: HttpServletRequest)
    extends HttpServletRequestWrapper(underlying) {

  // TODO: detect write operation from non main thread

  override val getAuthType: String = underlying.getAuthType

  override val getMethod: String = underlying.getMethod

  override val getPathInfo: String = underlying.getPathInfo

  override val getPathTranslated: String = underlying.getPathTranslated

  override val getContextPath: String = underlying.getContextPath

  override val getQueryString: String = underlying.getQueryString

  override val getRemoteUser: String = underlying.getRemoteUser

  override val getRequestedSessionId: String = underlying.getRequestedSessionId

  override val getRequestURI: String = underlying.getRequestURI

  override val getServletPath: String = underlying.getServletPath

  override val isRequestedSessionIdValid: Boolean = underlying.isRequestedSessionIdValid

  override val isRequestedSessionIdFromCookie: Boolean = underlying.isRequestedSessionIdFromCookie

  override val isRequestedSessionIdFromURL: Boolean = underlying.isRequestedSessionIdFromURL

  override val isRequestedSessionIdFromUrl: Boolean = underlying.isRequestedSessionIdFromURL

  override val getCharacterEncoding: String = underlying.getCharacterEncoding

  override val getContentLength: Int = underlying.getContentLength

  override val getContentType: String = underlying.getContentType

  override val getContentLengthLong: Long = underlying.getContentLengthLong

  override val getProtocol: String = underlying.getProtocol

  override val getServerName: String = underlying.getServerName

  override val getScheme: String = underlying.getScheme

  override val getServerPort: Int = underlying.getServerPort

  override val getRemoteAddr: String = underlying.getRemoteAddr

  override val getRemoteHost: String = underlying.getRemoteHost

  override val isSecure: Boolean = underlying.isSecure

  override val getRemotePort: Int = underlying.getRemotePort

  override val getLocalName: String = underlying.getLocalName

  override val getLocalAddr: String = underlying.getLocalAddr

  override val getLocalPort: Int = underlying.getLocalPort

  override val isAsyncStarted: Boolean = underlying.isAsyncStarted

  override val isAsyncSupported: Boolean = underlying.isAsyncSupported

}
