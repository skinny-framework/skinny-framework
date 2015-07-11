package org.scalatra.json

import scala.language.implicitConversions

import org.json4s._

trait JsonValueReaderProperty[T] { self: JsonMethods[T] =>

  implicit protected def jsonFormats: Formats
  protected implicit def jsonValueReader(d: JValue): JsonValueReader = new JsonValueReader(d)

}
