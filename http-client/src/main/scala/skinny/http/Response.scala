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

/**
 * HTTP/1.1 Response.
 */
case class Response(
    status: Int,
    headers: Map[String, String] = Map(),
    headerFields: Map[String, Seq[String]] = Map(),
    rawCookies: Map[String, String] = Map(),
    charset: Option[String] = None,
    body: Array[Byte] = Array()
) {

  def header(name: String): Option[String] = headers.get(name)
  def headerField(name: String): Seq[String] = headerFields.get(name).getOrElse(Nil)

  def asBytes: Array[Byte] = body

  def textBody: String = {
    Option(body).map { b =>
      charset match {
        case Some(c) => new String(b, c)
        case _ => new String(b)
      }
    }.orNull[String]
  }

  def asString: String = textBody

}
