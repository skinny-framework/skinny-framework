package skinny.engine.control

import scala.util.control.NoStackTrace

case class HaltException(
  status: Option[Int],
  reason: Option[String],
  headers: Map[String, String],
  body: Any)
    extends Throwable
    with NoStackTrace
