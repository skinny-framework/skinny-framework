package skinny.session.servlet

import javax.servlet.http.{ HttpServletRequest, HttpServletRequestWrapper, HttpServletResponse, HttpUpgradeHandler }
import javax.servlet.{ ServletRequest, ServletResponse }

/**
  * Http request wrapper for SkinnySession
  */
case class SkinnyHttpRequestWrapper(
    request: HttpServletRequest,
    session: SkinnyHttpSessionWrapper
) extends HttpServletRequestWrapper(request) {

  override def getSession(create: Boolean) = session // already created
  override def getSession                  = session

  override def getAuthType                    = request.getAuthType
  override def getCookies                     = request.getCookies
  override def getDateHeader(name: String)    = request.getDateHeader(name)
  override def getHeader(name: String)        = request.getHeader(name)
  override def getHeaders(name: String)       = request.getHeaders(name)
  override def getHeaderNames                 = request.getHeaderNames
  override def getIntHeader(name: String)     = request.getIntHeader(name)
  override def getMethod                      = request.getMethod
  override def getPathInfo                    = request.getPathInfo
  override def getPathTranslated              = request.getPathTranslated
  override def getContextPath                 = request.getContextPath
  override def getQueryString                 = request.getQueryString
  override def getRemoteUser                  = request.getRemoteUser
  override def isUserInRole(role: String)     = request.isUserInRole(role)
  override def getUserPrincipal()             = request.getUserPrincipal()
  override def getRequestedSessionId          = request.getRequestedSessionId
  override def getRequestURI                  = request.getRequestURI
  override def getRequestURL                  = request.getRequestURL
  override def getServletPath                 = request.getServletPath
  override def isRequestedSessionIdValid      = request.isRequestedSessionIdValid
  override def isRequestedSessionIdFromCookie = request.isRequestedSessionIdFromCookie
  override def isRequestedSessionIdFromURL    = request.isRequestedSessionIdFromURL
  // method isRequestedSessionIdFromUrl in trait HttpServletRequest is deprecated: see corresponding Javadoc for more information.
  override def isRequestedSessionIdFromUrl                 = request.isRequestedSessionIdFromURL
  override def authenticate(response: HttpServletResponse) = request.authenticate(response)
  override def login(username: String, password: String)   = request.login(username, password)
  override def logout()                                    = request.logout
  override def getParts                                    = request.getParts
  override def getPart(name: String)                       = request.getPart(name)
  override def getAttribute(name: String)                  = request.getAttribute(name)
  override def getAttributeNames                           = request.getAttributeNames
  override def getCharacterEncoding                        = request.getCharacterEncoding
  override def setCharacterEncoding(env: String)           = request.setCharacterEncoding(env)
  override def getContentLength                            = request.getContentLength
  override def getContentType                              = request.getContentType
  override def getInputStream                              = request.getInputStream
  override def getParameter(name: String)                  = request.getParameter(name)
  override def getParameterNames                           = request.getParameterNames
  override def getParameterValues(name: String)            = request.getParameterValues(name)
  override def getParameterMap                             = request.getParameterMap
  override def getProtocol                                 = request.getProtocol
  override def getScheme                                   = request.getScheme
  override def getServerName                               = request.getServerName
  override def getServerPort                               = request.getServerPort
  override def getReader                                   = request.getReader
  override def getRemoteAddr                               = request.getRemoteAddr
  override def getRemoteHost                               = request.getRemoteHost
  override def setAttribute(name: String, o: Any)          = request.setAttribute(name, o)
  override def removeAttribute(name: String)               = request.removeAttribute(name)
  override def getLocale                                   = request.getLocale
  override def getLocales                                  = request.getLocales
  override def isSecure                                    = request.isSecure
  override def getRequestDispatcher(path: String)          = request.getRequestDispatcher(path)
  // Deprecated. As of Version 2.1 of the Java Servlet API, use ServletContext#getRealPath instead.
  override def getRealPath(path: String) = request.getRealPath(path)
  override def getRemotePort             = request.getRemotePort
  override def getLocalName              = request.getLocalName
  override def getLocalAddr              = request.getLocalAddr
  override def getLocalPort              = request.getLocalPort
  override def getServletContext         = request.getServletContext
  override def startAsync()              = request.startAsync
  override def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse) =
    request.startAsync(servletRequest, servletResponse)
  override def isAsyncStarted    = request.isAsyncStarted
  override def isAsyncSupported  = request.isAsyncSupported
  override def getAsyncContext   = request.getAsyncContext
  override def getDispatcherType = request.getDispatcherType

  override def changeSessionId(): String                                   = request.changeSessionId()
  override def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T = request.upgrade(handlerClass)
  override def getContentLengthLong: Long                                  = request.getContentLengthLong

}
