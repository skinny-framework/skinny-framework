package skinny.exception

/**
 * Represents that request scope attributes or request scope itself is invalid.
 *
 * @param message message
 * @param cause cause
 */
case class RequestScopeConflictException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
