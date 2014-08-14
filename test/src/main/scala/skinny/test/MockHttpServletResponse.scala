/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import javax.servlet.http.{ Cookie, HttpServletResponse }
import javax.servlet.ServletOutputStream
import java.io.{ ByteArrayOutputStream, PrintWriter }
import java.util._
import org.mockito.Mockito._

class MockHttpServletResponse extends HttpServletResponse {

  var CHARSET_PREFIX = "charset="
  var CONTENT_TYPE_HEADER = "Content-Type"
  var CONTENT_LENGTH_HEADER = "Content-Length"
  var LOCATION_HEADER = "Location"

  //---------------------------------------------------------------------
  // ServletResponse properties
  //---------------------------------------------------------------------

  var outputStreamAccessAllowed = true
  var writerAccessAllowed = true
  var characterEncoding = "ISO-8859-1"
  var charset = false
  var content = new ByteArrayOutputStream()
  var writer: PrintWriter = _
  var contentLength: Long = 0
  var contentType: String = _
  var bufferSize = 4096
  var committed: Boolean = false
  var locale: Locale = Locale.getDefault()

  //---------------------------------------------------------------------
  // HttpServletResponse properties
  //---------------------------------------------------------------------

  var cookies = new ArrayList[Cookie]()
  var headers = new LinkedHashMap[String, HeaderValueHolder]
  var status = 200
  var errorMessage: String = _
  var forwardedUrl: String = _
  var includedUrls = new ArrayList[String]

  override def getLocale: Locale = locale

  override def setLocale(loc: Locale): Unit = {
    this.locale = locale
  }

  override def reset(): Unit = {
    resetBuffer()
    characterEncoding = null
    contentLength = 0
    contentType = null
    locale = null
    cookies.clear()
    headers.clear()
    status = HttpServletResponse.SC_OK
    errorMessage = null
  }

  override def isCommitted: Boolean = committed
  override def resetBuffer(): Unit = content.reset()
  override def flushBuffer(): Unit = committed = true
  override def getBufferSize: Int = bufferSize

  override def setBufferSize(size: Int): Unit = {
    this.bufferSize = bufferSize
  }
  override def setContentType(contentType: String): Unit = {
    this.contentType = contentType
  }
  override def setContentLength(len: Int): Unit = {
    this.contentLength = len
  }
  override def setCharacterEncoding(charset: String): Unit = {
    this.characterEncoding = charset
  }

  override def getWriter: PrintWriter = writer

  val stubOutputStream = new MockServletOutputStream
  override def getOutputStream: ServletOutputStream = stubOutputStream

  override def getContentType: String = contentType
  override def getCharacterEncoding: String = characterEncoding

  override def getStatus: Int = status
  override def setStatus(sc: Int, sm: String): Unit = {
    this.status = sc
    // TODO
  }
  override def setStatus(sc: Int): Unit = {
    this.status = sc
  }

  private def _addHeader(name: String, value: Any): Unit = {
    Option(headers.get(name).getValues()).map(_.add(value)).getOrElse(_setHeader(name, value))
  }
  private def _setHeader(name: String, value: Any): Unit = {
    headers.put(name, HeaderValueHolder(value))
  }

  override def getHeaderNames: Collection[String] = headers.keySet
  override def getHeaders(name: String): Collection[String] = headers.get(name).getStringValues
  override def getHeader(name: String): String = headers.get(name).getStringValue

  override def addHeader(name: String, value: String): Unit = _addHeader(name, value)
  override def setHeader(name: String, value: String): Unit = _setHeader(name, value)
  override def addIntHeader(name: String, value: Int): Unit = _addHeader(name, value)
  override def setIntHeader(name: String, value: Int): Unit = _setHeader(name, value)
  override def addDateHeader(name: String, date: Long): Unit = _addHeader(name, date)
  override def setDateHeader(name: String, date: Long): Unit = _setHeader(name, date)
  override def containsHeader(name: String): Boolean = headers.keySet.contains(name)

  override def sendRedirect(location: String): Unit = {
    setHeader(LOCATION_HEADER, location)
    setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY)
    committed = true
  }

  override def sendError(sc: Int): Unit = {
    status = sc
    committed = true
  }

  override def sendError(sc: Int, msg: String): Unit = {
    status = sc
    errorMessage = msg
    committed = true
  }

  override def encodeRedirectUrl(url: String): String = encodeRedirectURL(url)
  override def encodeUrl(url: String): String = encodeURL(url)
  override def encodeRedirectURL(url: String): String = encodeURL(url)
  override def encodeURL(url: String): String = url

  override def addCookie(cookie: Cookie): Unit = cookies.add(cookie)

  // TODO
  override def setContentLengthLong(len: Long): Unit = ???
}
