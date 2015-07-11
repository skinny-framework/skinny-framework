package org.scalatra

import scala.util.control.NoStackTrace

private[scalatra] case class HaltException(
  status: Option[Int],
  reason: Option[String],
  headers: Map[String, String],
  body: Any)
    extends Throwable
    with NoStackTrace
