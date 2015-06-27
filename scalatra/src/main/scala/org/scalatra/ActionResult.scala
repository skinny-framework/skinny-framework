package org.scalatra

case class ActionResult(
  status: ResponseStatus,
  body: Any,
  headers: Map[String, String])
