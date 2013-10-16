package skinny.exception

/**
 * Represents strong parameters issue.
 *
 * @param message message
 * @param cause cause
 */
case class StrongParametersException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
