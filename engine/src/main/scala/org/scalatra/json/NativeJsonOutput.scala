package org.scalatra.json

import java.io.Writer
import org.json4s._

import scala.text.Document

trait NativeJsonOutput extends JsonOutput[Document] with native.JsonMethods {

  protected def writeJson(json: JValue, writer: Writer) {
    if (json != JNothing) native.Printer.compact(render(json), writer)
  }

}