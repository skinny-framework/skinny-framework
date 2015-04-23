/*
 * Copyright 2011-2012 M3, Inc.
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

import scala.collection.mutable
import java.net.{ URL, HttpURLConnection }

/**
 * HTTP/1.1 Request.
 */
object Request {
  val X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded"
}

/**
 * HTTP/1.1 Request.
 */
case class Request(var url: String) {

  private[this] def returningThis(f: => Any): Request = {
    f
    this
  }

  var enableThrowingIOException: Boolean = false
  var followRedirects: Boolean = HttpURLConnection.getFollowRedirects

  var connectTimeoutMillis = HTTP.defaultConnectTimeoutMillis
  var readTimeoutMillis = HTTP.defaultReadTimeoutMillis

  var referer: Option[String] = None
  var userAgent: Option[String] = Some("skinny-http-client default user agent - http://skinny-framework.org/")
  var charset: Option[String] = Some("UTF-8")

  var headers: mutable.Map[String, String] = mutable.HashMap[String, String]()
  var queryParams: mutable.ListBuffer[QueryParam] = mutable.ListBuffer[QueryParam]()

  def requestBody: RequestBody = RequestBody(this)

  var bodyBytes: Option[Array[Byte]] = None
  var contentType: Option[String] = None
  var formParams: mutable.Map[String, Any] = mutable.HashMap[String, Any]()
  var multipartFormData: mutable.ListBuffer[FormData] = mutable.ListBuffer[FormData]()

  def enableThrowingIOException(enableThrowingIOException: Boolean): Request = returningThis {
    this.enableThrowingIOException = enableThrowingIOException
  }
  def url(url: String): Request = returningThis { this.url = url }
  def followRedirects(followRedirects: Boolean): Request = returningThis { this.followRedirects = followRedirects }

  def connectTimeoutMillis(millis: Int): Request = returningThis { this.connectTimeoutMillis = millis }
  def readTimeoutMillis(millis: Int): Request = returningThis { this.readTimeoutMillis = millis }

  def referer(referer: String): Request = returningThis { this.referer = Option(referer) }
  def userAgent(ua: String): Request = returningThis { this.userAgent = Option(ua) }
  def charset(charset: String): Request = returningThis { this.charset = Option(charset) }

  def headerNames: Set[String] = headers.keySet.toSet
  def header(name: String): Option[String] = headers.get(name)
  def header(name: String, value: String): Request = returningThis { headers += name -> value }

  def queryParams(params: (String, Any)*): Request = returningThis { params.foreach(p => queryParam(p)) }
  def queryParam(param: (String, Any)): Request = returningThis {
    queryParams += QueryParam(param._1, param._2)
  }
  def formParams(params: (String, Any)*): Request = returningThis {
    params.foreach { case (k, v) => formParams.update(k, v) }
  }

  def body(body: Array[Byte], contentType: String = Request.X_WWW_FORM_URLENCODED): Request = returningThis {
    bodyBytes = Some(body)
    this.contentType = Some(contentType)
  }

  def contentType(contentType: String): Request = returningThis {
    this.contentType = Some(contentType)
  }

  def multipartFormData(formData: Seq[FormData]): Request = returningThis {
    multipartFormData ++= formData
  }

  def toHttpURLConnection(method: Method): HttpURLConnection = {
    if (!queryParams.isEmpty) {
      for (queryParam <- queryParams) {
        if (queryParam.value != null) {
          val name: String = queryParam.name
          val value: String = String.valueOf(queryParam.value)
          val newParam: String = HTTP.urlEncode(name) + "=" + HTTP.urlEncode(value)
          url += (if (url.contains("?")) "&" else "?") + newParam
        }
      }
    }
    val conn: HttpURLConnection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod(method.name)
    conn.setConnectTimeout(connectTimeoutMillis)
    conn.setReadTimeout(readTimeoutMillis)
    conn.setInstanceFollowRedirects(followRedirects)
    userAgent.foreach(ua => conn.setRequestProperty("User-Agent", ua))
    headers.foreach { case (k, v) => conn.setRequestProperty(k, v) }
    conn
  }

}
