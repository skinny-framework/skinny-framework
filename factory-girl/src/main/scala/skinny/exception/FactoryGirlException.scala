package skinny.exception

/**
  * Represents FactoryGirl's failure.
  *
  * @param message message
  * @param cause cause
  */
case class FactoryGirlException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
