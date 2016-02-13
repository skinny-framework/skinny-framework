package skinny.test

/**
 * Mocked org.scalatra.HaltException.
 */
case class MockHaltException(
  status: Option[Int],
  reason: Option[String],
  headers: Map[String, String],
  body: Any
) extends Throwable