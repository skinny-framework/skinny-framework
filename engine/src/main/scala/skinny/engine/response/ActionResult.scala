package skinny.engine.response

case class ActionResult(
  status: ResponseStatus,
  body: Any,
  headers: Map[String, String])
