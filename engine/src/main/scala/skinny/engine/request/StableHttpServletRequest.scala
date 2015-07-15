package skinny.engine.request

import java.security.Principal
import javax.servlet.http._
import scala.collection.JavaConverters._

import scala.util.Try

/**
 * Stable HttpServletRequest
 *
 * HttpServletRequest object can be recycled.
 *
 * see also: https://github.com/scalatra/scalatra/pull/514
 * see also: http://jetty.4.x6.nabble.com/jetty-users-getContextPath-returns-null-td4962387.html
 * see also: https://bugs.eclipse.org/bugs/show_bug.cgi?id=433321
 */
class StableHttpServletRequest(
  private val underlying: HttpServletRequest)
    extends HttpServletRequestWrapper(underlying) {

  private[this] def tryOriginalFirst[A](action: => A, fallback: A): A = {
    val tried = Try(action)
    if (tried.isFailure || tried.filter(_ != null).toOption.isEmpty) fallback
    else tried.get
  }

  private[this] val _getAuthType = underlying.getAuthType

  override def getAuthType: String = tryOriginalFirst(underlying.getAuthType, _getAuthType)

  private[this] val _getMethod = underlying.getMethod

  override def getMethod: String = tryOriginalFirst(underlying.getMethod, _getMethod)

  private[this] val _getPathInfo = underlying.getPathInfo

  override def getPathInfo: String = tryOriginalFirst(underlying.getPathInfo, _getPathInfo)

  private[this] val _getPathTranslated = underlying.getPathTranslated

  override def getPathTranslated: String = tryOriginalFirst(underlying.getPathTranslated, _getPathTranslated)

  private[this] val _getContextPath = underlying.getContextPath

  override def getContextPath: String = tryOriginalFirst(underlying.getContextPath, _getContextPath)

  private[this] val _getQueryString = underlying.getQueryString

  override def getQueryString: String = tryOriginalFirst(underlying.getQueryString, _getQueryString)

  private[this] val _getRemoteUser = underlying.getRemoteUser

  override def getRemoteUser: String = tryOriginalFirst(underlying.getRemoteUser, _getRemoteUser)

  private[this] val _getRequestedSessionId = underlying.getRequestedSessionId

  override def getRequestedSessionId: String = tryOriginalFirst(underlying.getRequestedSessionId, _getRequestedSessionId)

  private[this] val _getRequestURI = underlying.getRequestURI

  override def getRequestURI: String = tryOriginalFirst(underlying.getRequestURI, _getRequestURI)

  private[this] val _getServletPath = underlying.getServletPath

  override def getServletPath: String = tryOriginalFirst(underlying.getServletPath, _getServletPath)

  private[this] val _isRequestedSessionIdValid = underlying.isRequestedSessionIdValid

  override def isRequestedSessionIdValid: Boolean = tryOriginalFirst(underlying.isRequestedSessionIdValid, _isRequestedSessionIdValid)

  private[this] val _isRequestedSessionIdFromCookie = underlying.isRequestedSessionIdFromCookie

  override def isRequestedSessionIdFromCookie: Boolean = tryOriginalFirst(underlying.isRequestedSessionIdFromCookie, _isRequestedSessionIdFromCookie)

  private[this] val _isRequestedSessionIdFromURL = underlying.isRequestedSessionIdFromURL

  override def isRequestedSessionIdFromURL: Boolean = tryOriginalFirst(underlying.isRequestedSessionIdFromURL, _isRequestedSessionIdFromURL)

  override def isRequestedSessionIdFromUrl: Boolean = isRequestedSessionIdFromURL

  private[this] val _getCharacterEncoding = underlying.getCharacterEncoding

  override def getCharacterEncoding: String = tryOriginalFirst(underlying.getCharacterEncoding, _getCharacterEncoding)

  private[this] val _getContentLength = underlying.getContentLength

  override def getContentLength: Int = tryOriginalFirst(underlying.getContentLength, _getContentLength)

  private[this] val _getContentType = underlying.getContentType

  override def getContentType: String = tryOriginalFirst(underlying.getContentType, _getContentType)

  private[this] val _getContentLengthLong = underlying.getContentLengthLong

  override def getContentLengthLong: Long = tryOriginalFirst(underlying.getContentLengthLong, _getContentLengthLong)

  private[this] val _getProtocol = underlying.getProtocol

  override def getProtocol: String = tryOriginalFirst(underlying.getProtocol, _getProtocol)

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getServerName = Try(underlying.getServerName).getOrElse(null)

  override def getServerName: String = tryOriginalFirst(underlying.getServerName, _getServerName)

  private[this] val _getScheme = underlying.getScheme

  override def getScheme: String = tryOriginalFirst(underlying.getScheme, _getScheme)

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getServerPort = Try(underlying.getServerPort).getOrElse(-1)

  override def getServerPort: Int = tryOriginalFirst(underlying.getServerPort, _getServerPort)

  private[this] val _getRemoteAddr = underlying.getRemoteAddr

  override def getRemoteAddr: String = tryOriginalFirst(underlying.getRemoteAddr, _getRemoteAddr)

  private[this] val _getRemoteHost = underlying.getRemoteHost

  override def getRemoteHost: String = tryOriginalFirst(underlying.getRemoteHost, _getRemoteHost)

  private[this] val _isSecure = underlying.isSecure

  override def isSecure: Boolean = tryOriginalFirst(underlying.isSecure, _isSecure)

  private[this] val _getRemotePort = underlying.getRemotePort

  override def getRemotePort: Int = tryOriginalFirst(underlying.getRemotePort, _getRemotePort)

  private[this] val _getLocalName = underlying.getLocalName

  override def getLocalName: String = tryOriginalFirst(underlying.getLocalName, _getLocalName)

  private[this] val _getLocalAddr = underlying.getLocalAddr

  override def getLocalAddr: String = tryOriginalFirst(underlying.getLocalAddr, _getLocalAddr)

  private[this] val _getLocalPort = underlying.getLocalPort

  override def getLocalPort: Int = tryOriginalFirst(underlying.getLocalPort, _getLocalPort)

  private[this] val _isAsyncStarted = underlying.isAsyncStarted

  override def isAsyncStarted: Boolean = tryOriginalFirst(underlying.isAsyncStarted, _isAsyncStarted)

  private[this] val _isAsyncSupported = underlying.isAsyncSupported

  override def isAsyncSupported: Boolean = tryOriginalFirst(underlying.isAsyncSupported, _isAsyncSupported)

  private[this] val _getHeaderNames = underlying.getHeaderNames

  override def getHeaderNames: java.util.Enumeration[String] = tryOriginalFirst(underlying.getHeaderNames, _getHeaderNames)

  private[this] val _cachedGetHeader: Map[String, String] = {
    Option(underlying.getHeaderNames)
      .map(_.asScala.map(name => name -> underlying.getHeader(name)).filterNot { case (_, v) => v == null }.toMap)
      .getOrElse(Map.empty)
  }

  private[this] val _cachedGetHeaders: Map[String, java.util.Enumeration[String]] = {
    Option(underlying.getHeaderNames)
      .map(_.asScala.map(name => name -> underlying.getHeaders(name)).filterNot { case (_, v) => v == null }.toMap)
      .getOrElse(Map.empty)
  }

  override def getHeader(name: String): String = tryOriginalFirst(underlying.getHeader(name), _cachedGetHeader.get(name).orNull[String])

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getRequestURL = Try(underlying.getRequestURL).getOrElse(new StringBuffer)

  override def getRequestURL: StringBuffer = tryOriginalFirst(underlying.getRequestURL, _getRequestURL)

  private[this] val _getCookies = underlying.getCookies

  override def getCookies: Array[Cookie] = tryOriginalFirst(underlying.getCookies, _getCookies)

  private[this] val _getUserPrincipal = underlying.getUserPrincipal

  override def getUserPrincipal: Principal = tryOriginalFirst(underlying.getUserPrincipal, _getUserPrincipal)

  override def getIntHeader(name: String): Int = {
    tryOriginalFirst(underlying.getIntHeader(name),
      // an integer expressing the value of the request header or -1 if the request doesn't have a header of this name
      _cachedGetHeader.get(name).map(_.toInt).getOrElse(-1))
  }

  override def getHeaders(name: String): java.util.Enumeration[String] = {
    tryOriginalFirst(underlying.getHeaders(name),
      // If the request does not have any headers of that name return an empty enumeration
      _cachedGetHeaders.get(name).getOrElse(java.util.Collections.emptyEnumeration[String]()))
  }

  override def getDateHeader(name: String): Long = {
    tryOriginalFirst(underlying.getDateHeader(name),
      // -1 if the named header was not included with the request
      _cachedGetHeader.get(name).map(_.toLong).getOrElse(-1L))
  }

  // Don't override getParts
  // javax.servlet.ServletException: Content-Type != multipart/form-data

  //  private[this] val _getParts = underlying.getParts
  //  override def getParts: java.util.Collection[Part] = tryOriginalFirst(underlying.getParts, _getParts)
  //  override def getPart(name: String): Part = {
  //    tryOriginalFirst(underlying.getPart(name), _getParts.asScala.find(_.getName == name).orNull)
  //  }

  // override def changeSessionId(): String = underlying.changeSessionId()
  // override def authenticate(response: HttpServletResponse): Boolean = underlying.authenticate(response)
  // override def logout(): Unit = underlying.logout()
  // override def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T = underlying.upgrade(handlerClass)

  // override def getSession(create: Boolean): HttpSession = underlying.getSession(create)
  // override def getSession: HttpSession = underlying.getSession

  // override def isUserInRole(role: String): Boolean = underlying.isUserInRole(role)
  // override def login(username: String, password: String): Unit = underlying.login(username, password)

}

object StableHttpServletRequest {

  def apply(req: HttpServletRequest): StableHttpServletRequest = {
    if (req == null) {
      throw new IllegalStateException("Use AsyncResult { ... } or futureWithContext { implicit ctx => ... } instead.")
    } else if (req.isInstanceOf[StableHttpServletRequest]) {
      req.asInstanceOf[StableHttpServletRequest]
    } else {
      new StableHttpServletRequest(req)
    }
  }
}
