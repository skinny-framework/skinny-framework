package org.scalatra.json

import org.json4s._

import scala.text.Document

trait NativeJsonValueReaderProperty
    extends JsonValueReaderProperty[Document] { self: native.JsonMethods =>

}
