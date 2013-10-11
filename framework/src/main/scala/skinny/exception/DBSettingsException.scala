package skinny.exception

case class DBSettingsException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
