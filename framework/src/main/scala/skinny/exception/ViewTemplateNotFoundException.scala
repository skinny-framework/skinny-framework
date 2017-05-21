package skinny.exception

/**
  * Represents view template issue.
  *
  * @param message message
  * @param cause cause
  */
case class ViewTemplateNotFoundException(message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)
