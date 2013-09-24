package skinny.exception

case class ViewTemplateNotFoundException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
