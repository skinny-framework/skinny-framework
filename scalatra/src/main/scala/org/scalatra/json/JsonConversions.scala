package org.scalatra.json

import java.util.Date

import org.scalatra.util.conversion.TypeConverter

object JsonConversions {

  class JsonValConversion[JValue](source: JValue) {
    private type JsonTypeConverter[T] = TypeConverter[JValue, T]
    def as[T: JsonTypeConverter]: Option[T] = implicitly[TypeConverter[JValue, T]].apply(source)
  }

  class JsonDateConversion[JValue](source: JValue, jsonToDate: String => TypeConverter[JValue, Date]) {
    def asDate(format: String): Option[Date] = jsonToDate(format).apply(source)
  }

}
