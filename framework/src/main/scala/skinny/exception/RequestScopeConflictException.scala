package skinny.exception

case class RequestScopeConflictException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
