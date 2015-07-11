package org.scalatra.json

import java.io.{ InputStream, InputStreamReader }

import com.fasterxml.jackson.databind.DeserializationFeature
import org.json4s._
import org.scalatra.util.RicherString._

trait JacksonJsonSupport extends JsonSupport[JValue] with JacksonJsonOutput with JValueResult {

  mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, jsonFormats.wantsBigDecimal)

  protected def readJsonFromStreamWithCharset(stream: InputStream, charset: String): JValue = {
    val rdr = new InputStreamReader(stream, charset)
    if (rdr.ready()) mapper.readValue(rdr, classOf[JValue])
    else {
      rdr.close()
      JNothing
    }
  }

  protected def readJsonFromBody(bd: String): JValue = {
    if (bd.nonBlank) mapper.readValue(bd, classOf[JValue])
    else JNothing
  }
}
