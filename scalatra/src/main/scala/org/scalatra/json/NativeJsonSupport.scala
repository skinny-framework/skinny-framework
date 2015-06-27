package org.scalatra.json

import java.io.{ InputStream, InputStreamReader }

import org.json4s._
import org.scalatra.util.RicherString._

import scala.text.Document

trait NativeJsonSupport extends JsonSupport[Document] with NativeJsonOutput with JValueResult {
  protected def readJsonFromStreamWithCharset(stream: InputStream, charset: String): JValue = {
    val rdr = new InputStreamReader(stream, charset)
    if (rdr.ready()) native.JsonParser.parse(rdr, jsonFormats.wantsBigDecimal)
    else {
      rdr.close()
      JNothing
    }
  }

  protected def readJsonFromBody(bd: String): JValue = {
    if (bd.nonBlank) native.JsonParser.parse(bd, jsonFormats.wantsBigDecimal)
    else JNothing
  }
}

