/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skinny.test

import java.io.BufferedReader
import java.security.Principal
import java.util._
import javax.servlet._
import javax.servlet.http._
import org.mockito.Mockito._
import scala.collection.JavaConverters._

class MockHttpServletRequest extends HttpServletRequest {

  var dispatcherType: DispatcherType = _

  override def getDispatcherType: DispatcherType = dispatcherType

  var asyncContext: AsyncContext = _
  var asyncSupported: Boolean    = true
  var asyncStarted: Boolean      = false

  override def getAsyncContext: AsyncContext = asyncContext
  override def isAsyncSupported: Boolean     = asyncSupported
  override def isAsyncStarted: Boolean       = asyncStarted
  override def startAsync(servletRequest: ServletRequest, servletResponse: ServletResponse): AsyncContext =
    asyncContext
  override def startAsync(): AsyncContext = asyncContext

  var servletContext: ServletContext = {
    val ctx = mock(classOf[ServletContext])
    when(ctx.getSessionCookieConfig).thenReturn(mock(classOf[SessionCookieConfig]))
    ctx
  }

  override def getServletContext: ServletContext = servletContext
  override def getRealPath(path: String): String = servletContext.getRealPath(path)

  var localPort: Int    = 80
  var localAddr: String = "127.0.0.1"
  var localName: String = "localhost"

  override def getLocalPort: Int    = localPort
  override def getLocalAddr: String = localAddr
  override def getLocalName: String = localName

  var serverPort: Int    = 80
  var serverName: String = "localhost"

  override def getServerPort: Int    = serverPort
  override def getServerName: String = serverName

  var remotePort: Int = 80
  var remoteHost      = "localhost"
  var remoteAddr      = "127.0.0.1"

  override def getRemotePort: Int    = remotePort
  override def getRemoteHost: String = remoteHost
  override def getRemoteAddr: String = remoteAddr

  override def getRequestDispatcher(path: String): RequestDispatcher = mock(classOf[RequestDispatcher])

  var secure: Boolean            = false
  override def isSecure: Boolean = secure

  var locales                                            = new LinkedList[Locale]
  override def getLocales: java.util.Enumeration[Locale] = Collections.enumeration(locales)
  override def getLocale: Locale                         = if (locales.size() > 0) locales.get(0) else null

  var attributes = new LinkedHashMap[String, AnyRef]

  override def getAttributeNames: java.util.Enumeration[String] = Collections.enumeration(attributes.keySet)
  override def getAttribute(name: String): AnyRef               = attributes.get(name)
  override def removeAttribute(name: String): Unit              = attributes.remove(name)
  override def setAttribute(name: String, value: AnyRef): Unit  = attributes.put(name, value)

  override def getReader: BufferedReader          = mock(classOf[BufferedReader])
  override def getInputStream: ServletInputStream = mock(classOf[ServletInputStream])

  var scheme   = "http"
  var protocol = "http"

  override def getScheme: String   = scheme
  override def getProtocol: String = protocol

  var parameters = new LinkedHashMap[String, Array[String]]

  override def getParameterMap: java.util.Map[String, Array[String]] = Collections.unmodifiableMap(parameters)
  override def getParameterValues(name: String): Array[String]       = Option(getParameterMap.get(name)).getOrElse(Array())
  override def getParameterNames: java.util.Enumeration[String]      = Collections.enumeration(getParameterMap.keySet)
  override def getParameter(name: String): String = {
    val params = getParameterMap.get(name)
    if (params != null && params.size > 0) params(0) else null
  }

  var content: Array[Byte]      = null
  var contentType: String       = null
  var characterEncoding: String = null

  override def getContentType: String = contentType
  override def getContentLength: Int  = if (content != null) content.length else -1
  override def setCharacterEncoding(env: String): Unit = {
    characterEncoding = env
    updateContentTypeHeader()
  }

  val CONTENT_TYPE_HEADER = "Content-Type"
  val CHARSET_PREFIX      = "charset="

  def updateContentTypeHeader() {
    if (contentType != null) {
      val s = new StringBuilder(contentType)
      if (!contentType.toLowerCase.contains(CHARSET_PREFIX) && characterEncoding != null) {
        s.append(";").append(CHARSET_PREFIX).append(characterEncoding)
      }
      doAddHeaderValue(CONTENT_TYPE_HEADER, s.toString(), true)
    }
  }

  val headers = new LinkedHashMap[String, HeaderValueHolder]

  def doAddHeaderValue(name: String, value: AnyRef, replace: Boolean) {
    def replaceHeader(name: String): HeaderValueHolder = {
      val h = new HeaderValueHolder
      headers.put(name, h)
      h
    }
    val header = HeaderValueHolder
      .getByName(headers, name)
      .map { h =>
        if (replace) replaceHeader(name) else h
      }
      .getOrElse { replaceHeader(name) }

    if (value.isInstanceOf[Collection[_]]) {
      value.asInstanceOf[Collection[AnyRef]].asScala.foreach { v =>
        header.addValue(v)
      }
    } else if (value.getClass.isArray) {
      value.asInstanceOf[Collection[AnyRef]].asScala.foreach { v =>
        header.addValue(v)
      }
    } else {
      header.addValue(value)
    }
  }

  override def getCharacterEncoding: String = characterEncoding

  val parts = new LinkedHashMap[String, Part]

  override def getPart(name: String): Part = null

  override def getParts: java.util.Collection[Part] = parts.values

  override def logout(): Unit = {
    userPrincipal = null
    remoteUser = null
    authType = null
  }

  override def login(username: String, password: String): Unit      = throw new UnsupportedOperationException
  override def authenticate(response: HttpServletResponse): Boolean = throw new UnsupportedOperationException

  var requestedSessionIdFromURL: Boolean = false

  override def isRequestedSessionIdFromUrl: Boolean = requestedSessionIdFromURL
  override def isRequestedSessionIdFromURL: Boolean = requestedSessionIdFromURL

  var requestedSessionIdFromCookie: Boolean = true

  override def isRequestedSessionIdFromCookie: Boolean = requestedSessionIdFromCookie

  var requestedSessionIdValid: Boolean = true

  override def isRequestedSessionIdValid: Boolean = requestedSessionIdValid

  val session = new MockHttpSession

  override def getSession: HttpSession                  = session
  override def getSession(create: Boolean): HttpSession = session

  var servletPath: String             = _
  override def getServletPath: String = servletPath

  override def getRequestURL: StringBuffer = {
    val url = new StringBuffer(this.scheme).append("://").append(this.serverName)
    if (this.serverPort > 0 &&
        (("http".equalsIgnoreCase(scheme) && this.serverPort != 80) || ("https".equalsIgnoreCase(scheme) && this.serverPort != 443))) {
      url.append(':').append(this.serverPort)
    }
    if (getRequestURI.exists(c => !Character.isWhitespace(c))) {
      url.append(getRequestURI)
    }
    url
  }

  var requestURI: String = _

  override def getRequestURI: String = requestURI

  var requestedSessionId: String = _

  override def getRequestedSessionId: String = requestedSessionId

  var userPrincipal: Principal = _

  override def getUserPrincipal: Principal = userPrincipal

  val userRoles = new HashSet[String]

  override def isUserInRole(role: String): Boolean = userRoles.contains(role)

  var remoteUser: String = _

  override def getRemoteUser: String = remoteUser

  var queryString: String = _

  override def getQueryString: String = queryString

  var contextPath: String = ""

  override def getContextPath: String = contextPath

  var pathInfo: String = _

  override def getPathInfo: String = pathInfo
  override def getPathTranslated: String = {
    if (pathInfo != null) getRealPath(pathInfo) else null
  }

  var method: String = _

  override def getMethod: String = method

  override def getIntHeader(name: String): Int = {
    HeaderValueHolder
      .getByName(headers, name)
      .map { header =>
        val i: Int = header.getValue() match {
          case null        => -1
          case n: Number   => n.intValue
          case str: String => Integer.valueOf(str)
          case v           => throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + v)
        }
        i
      }
      .getOrElse(-1)
  }

  override def getHeaderNames: java.util.Enumeration[String] = Collections.enumeration(headers.keySet)

  override def getHeaders(name: String): java.util.Enumeration[String] = {
    HeaderValueHolder
      .getByName(headers, name)
      .map { header =>
        Collections.enumeration(header.getStringValues)
      }
      .getOrElse {
        Collections.enumeration(new LinkedList[String])
      }
  }

  override def getHeader(name: String): String = {
    HeaderValueHolder.getByName(this.headers, name).map(_.getStringValue).orNull[String]
  }

  override def getDateHeader(name: String): Long = {
    HeaderValueHolder
      .getByName(this.headers, name)
      .map { header =>
        header.getValue match {
          case null      => -1L
          case d: Date   => d.getTime
          case n: Number => n.longValue
          case v =>
            throw new IllegalArgumentException("Value for header '" + name + "' is neither a Date nor a Number: " + v)
        }
      }
      .getOrElse(-1L)
  }

  var cookies = Array[Cookie]()

  override def getCookies: Array[Cookie] = cookies

  var authType: String = _

  override def getAuthType: String = authType

  override def changeSessionId(): String = throw new UnsupportedOperationException

  override def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T = throw new UnsupportedOperationException

  override def getContentLengthLong: Long = getContentLength.toLong
}
