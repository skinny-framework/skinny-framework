package skinny.session.servlet

import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }
import javax.servlet.{ ServletResponse, ServletRequest }

case class SkinnyHttpRequestWrapper(request: HttpServletRequest, session: SkinnyHttpSessionWrapper) extends HttpServletRequest {

  def getSession(create: Boolean) = session // already created
  def getSession = session

  def getAuthType = request.getAuthType
  def getCookies = request.getCookies
  def getDateHeader(name: String) = request.getDateHeader(name)
  def getHeader(name: String) = request.getHeader(name)
  def getHeaders(name: String) = request.getHeaders(name)
  def getHeaderNames = request.getHeaderNames
  def getIntHeader(name: String) = request.getIntHeader(name)
  def getMethod = request.getMethod
  def getPathInfo = request.getPathInfo
  def getPathTranslated = request.getPathTranslated
  def getContextPath = request.getContextPath
  def getQueryString = request.getQueryString
  def getRemoteUser = request.getRemoteUser
  def isUserInRole(role: String) = request.isUserInRole(role)
  def getUserPrincipal = request.getUserPrincipal
  def getRequestedSessionId = request.getRequestedSessionId
  def getRequestURI = request.getRequestURI
  def getRequestURL = request.getRequestURL
  def getServletPath = request.getServletPath
  def isRequestedSessionIdValid = request.isRequestedSessionIdValid
  def isRequestedSessionIdFromCookie = request.isRequestedSessionIdFromCookie
  def isRequestedSessionIdFromURL = request.isRequestedSessionIdFromURL
  def isRequestedSessionIdFromUrl = request.isRequestedSessionIdFromUrl
  def authenticate(response: HttpServletResponse) = request.authenticate(response)
  def login(username: String, password: String) = request.login(username, password)
  def logout() = request.logout
  def getParts = request.getParts
  def getPart(name: String) = request.getPart(name)
  def getAttribute(name: String) = request.getAttribute(name)
  def getAttributeNames = request.getAttributeNames
  def getCharacterEncoding = request.getCharacterEncoding
  def setCharacterEncoding(env: String) = request.setCharacterEncoding(env)
  def getContentLength = request.getContentLength
  def getContentType = request.getContentType
  def getInputStream = request.getInputStream
  def getParameter(name: String) = request.getParameter(name)
  def getParameterNames = request.getParameterNames
  def getParameterValues(name: String) = request.getParameterValues(name)
  def getParameterMap = request.getParameterMap
  def getProtocol = request.getProtocol
  def getScheme = request.getScheme
  def getServerName = request.getServerName
  def getServerPort = request.getServerPort
  def getReader = request.getReader
  def getRemoteAddr = request.getRemoteAddr
  def getRemoteHost = request.getRemoteHost
  def setAttribute(name: String, o: Any) = request.setAttribute(name, o)
  def removeAttribute(name: String) = request.removeAttribute(name)
  def getLocale = request.getLocale
  def getLocales = request.getLocales
  def isSecure = request.isSecure
  def getRequestDispatcher(path: String) = request.getRequestDispatcher(path)
  def getRealPath(path: String) = request.getRealPath(path)
  def getRemotePort = request.getRemotePort
  def getLocalName = request.getLocalName
  def getLocalAddr = request.getLocalAddr
  def getLocalPort = request.getLocalPort
  def getServletContext = request.getServletContext
  def startAsync() = request.startAsync
  def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse) = request.startAsync(servletRequest, servletResponse)
  def isAsyncStarted = request.isAsyncStarted
  def isAsyncSupported = request.isAsyncSupported
  def getAsyncContext = request.getAsyncContext
  def getDispatcherType = request.getDispatcherType

  // Servlet 3.1
  def changeSessionId(): String = request.changeSessionId()
  def upgrade[T <: javax.servlet.http.HttpUpgradeHandler](clazz: Class[T]): T = request.upgrade(clazz)
  def getContentLengthLong: Long = request.getContentLengthLong

}
