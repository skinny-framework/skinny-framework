package skinny.exception

case class StrongParametersException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
