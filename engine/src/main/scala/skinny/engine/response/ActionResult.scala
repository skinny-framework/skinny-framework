package skinny.engine.response

/**
 * skinny-engine's action result.
 */
case class ActionResult(
  status: ResponseStatus,
  body: Any,
  headers: Map[String, String])
