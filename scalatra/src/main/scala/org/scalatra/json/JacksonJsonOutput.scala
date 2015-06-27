package org.scalatra.json

import java.io.Writer
import org.json4s._

trait JacksonJsonOutput
    extends JsonOutput[JValue]
    with jackson.JsonMethods {

  protected def writeJson(json: JValue, writer: Writer) {
    if (json != JNothing) mapper.writeValue(writer, json)
  }

}
