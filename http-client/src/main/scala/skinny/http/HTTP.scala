/*
 * Copyright 2011-2012 M3, Inc.
 * Copyright 2013-2014 skinny-framework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package skinny.http

import java.io._
import java.net.{ URLEncoder, HttpURLConnection }
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ Future, ExecutionContext }
import scala.util.control.NonFatal
import skinny.logging.LoggerProvider
import skinny.util.LoanPattern.using

object HTTP extends HTTP {

  val DEFAULT_CHARSET = "UTF-8"
  private val RESPONSE_CONTENT_TYPE_REGEXP = "[^;]+;\\s*charset=(.+)".r

}

/**
 * HTTP/1.1
 */
class HTTP extends LoggerProvider {
  import HTTP._

  var defaultConnectTimeoutMillis: Int = 1000
  var defaultReadTimeoutMillis: Int = 5000

  type EC = ExecutionContext

  // -----
  // GET

  def get(req: Request): Response = request(Method.GET, req)
  def get(url: String, charset: String = DEFAULT_CHARSET): Response = get(Request(url).charset(charset))
  def get(url: String, queryParams: (String, Any)*): Response = get(Request(url).queryParams(queryParams: _*))

  def asyncGet(req: Request)(implicit ctx: EC): Future[Response] = Future(get(req))
  def asyncGet(url: String, charset: String = DEFAULT_CHARSET)(implicit ctx: EC): Future[Response] = Future(get(url, charset))
  def asyncGet(url: String, queryParams: (String, Any)*)(implicit ctx: EC): Future[Response] = Future(get(url, queryParams: _*))

  // -----
  // POST

  def post(req: Request): Response = request(Method.POST, req)
  def post(url: String, data: String): Response = post(Request(url).body(data.getBytes))
  def post(url: String, formParams: (String, Any)*): Response = post(Request(url).formParams(formParams: _*))
  def postMultipart(url: String, data: FormData*): Response = post(Request(url).multipartFormData(data))

  def asyncPost(req: Request)(implicit ctx: EC): Future[Response] = Future(post(req))
  def asyncPost(url: String, data: String)(implicit ctx: EC): Future[Response] = Future(post(url, data))
  def asyncPost(url: String, formParams: (String, Any)*)(implicit ctx: EC): Future[Response] = Future(post(url, formParams: _*))
  def asyncPostMultipart(url: String, data: FormData*)(implicit ctx: EC): Future[Response] = Future(postMultipart(url, data: _*))

  // -----
  // PUT

  def put(req: Request): Response = request(Method.PUT, req)
  def put(url: String, data: String): Response = put(Request(url).body(data.getBytes))
  def put(url: String, formParams: (String, Any)*): Response = put(Request(url).formParams(formParams: _*))
  def putMultipart(url: String, data: FormData*): Response = put(Request(url).multipartFormData(data))

  def asyncPut(req: Request)(implicit ctx: EC): Future[Response] = Future(put(req))
  def asyncPut(url: String, data: String)(implicit ctx: EC): Future[Response] = Future(put(url, data))
  def asyncPut(url: String, formParams: (String, Any)*)(implicit ctx: EC): Future[Response] = Future(put(url, formParams: _*))
  def asyncPutMultipart(url: String, data: FormData*)(implicit ctx: EC): Future[Response] = Future(putMultipart(url, data: _*))

  // -----
  // DELETE

  def delete(req: Request): Response = request(Method.DELETE, req)
  def delete(url: String): Response = delete(Request(url))

  def asyncDelete(req: Request)(implicit ctx: EC): Future[Response] = Future(delete(req))
  def asyncDelete(url: String)(implicit ctx: EC): Future[Response] = Future(delete(url))

  // -----
  // HEAD

  def head(req: Request): Response = request(Method.HEAD, req)
  def head(url: String): Response = head(Request(url))

  def asyncHead(req: Request)(implicit ctx: EC): Future[Response] = Future(head(req))
  def asyncHead(url: String)(implicit ctx: EC): Future[Response] = Future(head(url))

  // -----
  // OPTIONS

  def options(req: Request): Response = request(Method.OPTIONS, req)
  def options(url: String): Response = options(Request(url))

  def asyncOptions(req: Request)(implicit ctx: EC): Future[Response] = Future(options(req))
  def asyncOptions(url: String)(implicit ctx: EC): Future[Response] = Future(options(url))

  // -----
  // TRACE

  def trace(req: Request): Response = request(Method.TRACE, req)
  def trace(url: String): Response = trace(Request(url))

  def asyncTrace(req: Request)(implicit ctx: EC): Future[Response] = Future(trace(req))
  def asyncTrace(url: String)(implicit ctx: EC): Future[Response] = Future(trace(url))

  // -----
  // General request

  def asyncRequest(method: Method, req: Request)(implicit ctx: EC): Future[Response] = Future(request(method, req))

  def request(method: Method, request: Request): Response = {

    val conn: HttpURLConnection = request.toHttpURLConnection(method)
    conn.setRequestProperty("Connection", "close")
    request.charset.foreach(c => conn.setRequestProperty("Accept-Charset", c))

    var needToThrowException: Boolean = false
    var exceptionMessage: Option[String] = None
    var inputStream: Option[InputStream] = None
    try {
      try {
        if (request.bodyBytes.isDefined) {
          conn.setDoOutput(true)
          request.contentType.foreach(ct => conn.setRequestProperty("Content-Type", ct))
          using(conn.getOutputStream) { out => request.requestBody.asBytes.foreach(b => out.write(b)) }
        } else if (!request.formParams.isEmpty) {
          conn.setDoOutput(true)
          conn.setRequestProperty("Content-Type", Request.X_WWW_FORM_URLENCODED)
          using(conn.getOutputStream)(_.write(request.requestBody.asApplicationXWwwFormUrlencoded))
        } else if (!request.multipartFormData.isEmpty) {
          conn.setDoOutput(true)
          val boundary = "----SkinnyHTTPClientBoundary_" + System.currentTimeMillis
          conn.setRequestProperty("Content-Type", s"multipart/form-data; boundary=${boundary}")
          using(conn.getOutputStream) { out => out.write(request.requestBody.asMultipart(boundary)) }
        }

        logger.debug {
          s"""
          |- HTTP Request started. -
          |
          | ${method.name} ${request.url}
          |
          | Charset: ${request.charset.getOrElse("")}
          | Content-Type: ${request.contentType.getOrElse("")}
          | Referer: ${request.referer.getOrElse("")}
          | User-Agent: ${request.userAgent.getOrElse("")}
          |${request.headerNames.map(name => s" ${name}: ${request.header(name).getOrElse("")}").mkString("\n")}
          |---------
          |""".stripMargin
        }

        conn.connect()
        inputStream = Option(conn.getInputStream)

      } catch {
        case e: IOException =>
          val message = s"${method} ${request.url} failed because ${e.getMessage}"
          logger.warn(message)
          logger.debug(message, e)
          if (request.enableThrowingIOException) {
            needToThrowException = true
            exceptionMessage = Option(e.getMessage)
          }
          inputStream = Option(conn.getErrorStream)
      }

      val response: Response = Response(
        status = conn.getResponseCode,
        charset = {
        Option(conn.getHeaderField("Content-Type")).map { contentType =>
          contentType.toLowerCase match {
            case RESPONSE_CONTENT_TYPE_REGEXP(charset) => Some(charset)
            case _ => None
          }
        }.getOrElse(request.charset)
      },
        headerFields = conn.getHeaderFields.asScala.map { case (k, v) => k -> v.asScala }.toMap,
        headers = conn.getHeaderFields.keySet.asScala.map(name => name -> conn.getHeaderField(name)).toMap,
        rawCookies = Option(conn.getHeaderFields.get("Set-Cookie")).map { setCookies =>
          setCookies.asScala.flatMap { setCookie =>
            setCookie.split("=", 2) match {
              case Array(name, _) => Some(name -> setCookie)
              case _ => None
            }
          }.toMap
        }.getOrElse(Map()),
        body = inputStream.map { is =>
          using(is) { input =>
            using(new ByteArrayOutputStream) { out =>
              var c: Int = 0
              while ({ c = input.read(); c } != -1) { out.write(c) }
              out.toByteArray
            }
          }
        }.getOrElse(Array())
      )

      logger.debug {
        s"""
          |- HTTP Request finished. -
          |
          | ${method.name} ${request.url}
          |
          | Status: ${response.status}
          | Charset: ${response.charset.getOrElse("")}
          |${response.headers.filter(_._1 != null).map { case (k, v) => s" ${k}: ${v}" }.mkString("\n")}
          |---------
          |""".stripMargin
      }

      if (needToThrowException) throw new HTTPException(exceptionMessage, response)
      else response

    } finally {
      inputStream.foreach { s =>
        try s.close
        catch {
          case NonFatal(e) =>
            logger.debug("Error when closing stream because {}", e.getMessage, e)
        }
      }
      conn.disconnect
    }
  }

  def urlEncode(rawValue: String): String = urlEncode(rawValue, DEFAULT_CHARSET)

  def urlEncode(rawValue: String, charset: String): String = {
    try URLEncoder.encode(rawValue, charset)
    catch {
      case e: UnsupportedEncodingException =>
        throw new IllegalStateException(e.getMessage, e)
    }
  }

}
