package skinny.engine.request

import java.io.BufferedReader
import java.security.Principal
import java.util.Locale
import javax.servlet._
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

  var stopTryingOriginal: Boolean = false

  private[this] def tryOriginalFirst[A](action: => A, fallback: A): A = {
    if (stopTryingOriginal) {
      fallback
    } else {
      val tried = Try(action)
      if (tried.isFailure || tried.filter(_ != null).toOption.isEmpty) fallback
      else tried.get
    }
  }

  // -------------------------
  // context, fixed request metadata

  override def getServletContext: ServletContext = underlying.getServletContext

  // AsyncContext must not be cached
  override def getAsyncContext: AsyncContext = underlying.getAsyncContext

  override def startAsync(): AsyncContext = underlying.startAsync()
  override def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse): AsyncContext = {
    underlying.startAsync(servletRequest, servletResponse)
  }

  private[this] val _getRequest = super.getRequest
  override def getRequest: ServletRequest = tryOriginalFirst(super.getRequest, _getRequest)
  override def setRequest(request: ServletRequest): Unit = super.setRequest(request)

  override def isWrapperFor(wrapped: ServletRequest): Boolean = super.isWrapperFor(wrapped)
  override def isWrapperFor(wrappedType: Class[_]): Boolean = super.isWrapperFor(wrappedType)

  private[this] val _getDispatcherType = underlying.getDispatcherType
  override def getDispatcherType: DispatcherType = _getDispatcherType

  override def getReader: BufferedReader = underlying.getReader

  override def getInputStream: ServletInputStream = underlying.getInputStream

  private[this] val _getAuthType = underlying.getAuthType
  override def getAuthType: String = _getAuthType

  private[this] val _getMethod = underlying.getMethod
  override def getMethod: String = _getMethod

  private[this] val _getPathInfo = underlying.getPathInfo
  override def getPathInfo: String = _getPathInfo

  private[this] val _getPathTranslated = underlying.getPathTranslated
  override def getPathTranslated: String = _getPathTranslated

  private[this] val _getContextPath = underlying.getContextPath
  override def getContextPath: String = _getContextPath

  private[this] val _getQueryString = underlying.getQueryString
  override def getQueryString: String = _getQueryString

  private[this] val _getRemoteUser = underlying.getRemoteUser
  override def getRemoteUser: String = _getRemoteUser

  private[this] val _getRequestedSessionId = underlying.getRequestedSessionId
  override def getRequestedSessionId: String = _getRequestedSessionId

  private[this] val _getRequestURI = underlying.getRequestURI
  override def getRequestURI: String = _getRequestURI

  private[this] val _getServletPath = underlying.getServletPath
  override def getServletPath: String = _getServletPath

  private[this] val _isRequestedSessionIdValid = underlying.isRequestedSessionIdValid
  override def isRequestedSessionIdValid: Boolean = _isRequestedSessionIdValid

  private[this] val _isRequestedSessionIdFromCookie = underlying.isRequestedSessionIdFromCookie
  override def isRequestedSessionIdFromCookie: Boolean = _isRequestedSessionIdFromCookie

  private[this] val _isRequestedSessionIdFromURL = underlying.isRequestedSessionIdFromURL
  override def isRequestedSessionIdFromURL: Boolean = _isRequestedSessionIdFromURL
  override def isRequestedSessionIdFromUrl: Boolean = isRequestedSessionIdFromURL

  private[this] val _getCharacterEncoding = underlying.getCharacterEncoding
  override def getCharacterEncoding: String = tryOriginalFirst(underlying.getCharacterEncoding, _getCharacterEncoding)
  override def setCharacterEncoding(enc: String): Unit = underlying.setCharacterEncoding(enc)

  private[this] val _getContentLength = underlying.getContentLength
  override def getContentLength: Int = _getContentLength

  private[this] val _getContentType = underlying.getContentType
  override def getContentType: String = _getContentType

  private[this] val _getContentLengthLong = underlying.getContentLengthLong
  override def getContentLengthLong: Long = _getContentLengthLong

  private[this] val _getProtocol = underlying.getProtocol
  override def getProtocol: String = _getProtocol

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getServerName = Try(underlying.getServerName).getOrElse(null)
  override def getServerName: String = _getServerName

  private[this] val _getScheme = underlying.getScheme
  override def getScheme: String = _getScheme

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getServerPort = Try(underlying.getServerPort).getOrElse(-1)
  override def getServerPort: Int = _getServerPort

  private[this] val _getRemoteAddr = underlying.getRemoteAddr
  override def getRemoteAddr: String = _getRemoteAddr

  private[this] val _getRemoteHost = underlying.getRemoteHost
  override def getRemoteHost: String = _getRemoteHost

  private[this] val _isSecure = underlying.isSecure
  override def isSecure: Boolean = _isSecure

  private[this] val _getRemotePort = underlying.getRemotePort
  override def getRemotePort: Int = _getRemotePort

  private[this] val _getLocalName = underlying.getLocalName
  override def getLocalName: String = _getLocalName

  private[this] val _getLocalAddr = underlying.getLocalAddr
  override def getLocalAddr: String = _getLocalAddr

  private[this] val _getLocalPort = underlying.getLocalPort
  override def getLocalPort: Int = _getLocalPort

  private[this] val _isAsyncStarted = underlying.isAsyncStarted
  override def isAsyncStarted: Boolean = _isAsyncStarted

  private[this] val _isAsyncSupported = underlying.isAsyncSupported
  override def isAsyncSupported: Boolean = _isAsyncSupported

  // java.lang.IllegalStateException: No uri on Jetty when testing
  private[this] val _getRequestURL = Try(underlying.getRequestURL).getOrElse(new StringBuffer)
  override def getRequestURL: StringBuffer = _getRequestURL

  private[this] val _getCookies = underlying.getCookies
  override def getCookies: Array[Cookie] = _getCookies

  private[this] val _getUserPrincipal = underlying.getUserPrincipal
  override def getUserPrincipal: Principal = _getUserPrincipal

  // should not cache the value: java.lang.IllegalStateException: No SessionManager
  override def getSession: HttpSession = underlying.getSession
  override def getSession(create: Boolean): HttpSession = underlying.getSession(create)

  override def changeSessionId(): String = underlying.changeSessionId()
  override def authenticate(response: HttpServletResponse): Boolean = underlying.authenticate(response)
  override def logout(): Unit = underlying.logout()
  override def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T = underlying.upgrade(handlerClass)

  override def isUserInRole(role: String): Boolean = underlying.isUserInRole(role)
  override def login(username: String, password: String): Unit = underlying.login(username, password)

  // Don't override getParts
  // javax.servlet.ServletException: Content-Type != multipart/form-data

  //  private[this] val _getParts = underlying.getParts
  //  override def getParts: java.util.Collection[Part] = tryOriginalFirst(underlying.getParts, _getParts)
  //  override def getPart(name: String): Part = {
  //    tryOriginalFirst(underlying.getPart(name), _getParts.asScala.find(_.getName == name).orNull)
  //  }

  // deprecated
  // override def getRealPath(path: String): String = underlying.getRealPath(path)

  // -------------------------
  // parameters

  private[this] val _getParameterNames = underlying.getParameterNames
  private[this] val _getParameterMap = underlying.getParameterMap

  override def getParameterNames: java.util.Enumeration[String] = _getParameterNames
  override def getParameterMap: java.util.Map[String, Array[String]] = _getParameterMap
  override def getParameter(name: String): String = getParameterMap.get(name).headOption.orNull[String]
  override def getParameterValues(name: String): Array[String] = getParameterMap.get(name)

  override def getRequestDispatcher(path: String): RequestDispatcher = underlying.getRequestDispatcher(path)

  // -------------------------
  // locale

  private[this] val _getLocale = underlying.getLocale
  private[this] val _getLocales = underlying.getLocales

  override def getLocale: Locale = _getLocale
  override def getLocales: java.util.Enumeration[Locale] = _getLocales

  // -------------------------
  // attributes

  private[this] val _getAttributeNames = underlying.getAttributeNames
  private[this] val _attributes: Map[String, AnyRef] = {
    Option(underlying.getAttributeNames)
      .map(_.asScala.map(name => name -> underlying.getAttribute(name)).filterNot { case (_, v) => v == null }.toMap)
      .getOrElse(Map.empty)
  }

  override def getAttributeNames: java.util.Enumeration[String] = tryOriginalFirst(underlying.getAttributeNames, _getAttributeNames)
  override def getAttribute(name: String): AnyRef = tryOriginalFirst(underlying.getAttribute(name), _attributes.getOrElse(name, null))
  override def setAttribute(name: String, o: scala.Any): Unit = underlying.setAttribute(name, o)
  override def removeAttribute(name: String): Unit = underlying.removeAttribute(name)

  // -------------------------
  // headers

  private[this] val _getHeaderNames = underlying.getHeaderNames
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

  // this API must try original first
  override def getHeaderNames: java.util.Enumeration[String] = tryOriginalFirst(underlying.getHeaderNames, _getHeaderNames)
  override def getHeader(name: String): String = {
    tryOriginalFirst(underlying.getHeader(name),
      _cachedGetHeader.get(name).orNull[String])
  }
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
