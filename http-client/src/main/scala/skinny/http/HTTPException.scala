package skinny.http

import java.io.IOException

/**
 * Exception which represents errors when Request#enableThrowingIOException is true.
 *
 * @param message message
 * @param response response
 */
case class HTTPException(message: Option[String], response: Response)
  extends IOException(message.orNull[String])