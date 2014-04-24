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

import java.io.ByteArrayOutputStream
import skinny.util.LoanPattern.using

/**
 * Request body.
 */
case class RequestBody(request: Request) {

  private[this] val CRLF = "\r\n"

  def asBytes: Array[Byte] = request.bodyBytes.getOrElse(Array())

  def asApplicationXWwwFormUrlencoded: Array[Byte] = {
    val encoded = request.formParams.flatMap {
      case (key, value) =>
        if (value != null) Some(s"${HTTP.urlEncode(key)}=${HTTP.urlEncode(String.valueOf(value))}")
        else None
    }.mkString("&")

    encoded.getBytes
  }

  def asMultipart(boundary: String): Array[Byte] = {
    using(new ByteArrayOutputStream) { out =>
      request.multipartFormData.foreach { data =>
        {
          val sb = new StringBuilder
          sb.append("--").append(boundary).append(CRLF)
          sb.append("Content-Disposition: form-data; name=").append('"').append(data.name).append('"')
          data.filename.foreach { name =>
            sb.append("; filename=").append('"').append(name).append('"')
          }
          sb.append(CRLF)
          data.contentType.foreach { contentType =>
            sb.append("content-type: ").append(contentType).append(CRLF)
          }
          sb.append(CRLF)
          out.write(sb.toString.getBytes)
        }
        Option(data.asBytes).foreach { bytes =>
          (0 until bytes.length).foreach { i => out.write(bytes(i)) }
        }
        out.write(CRLF.getBytes)

        {
          val sb = new StringBuilder
          sb.append("--").append(boundary).append("--").append(CRLF)
          out.write(sb.toString.getBytes)
        }
      }
      out.toByteArray
    }
  }

}