package org.scalatra.json

import org.json4s._

trait JacksonJsonValueReaderProperty
    extends JsonValueReaderProperty[JValue] { self: jackson.JsonMethods =>

}
