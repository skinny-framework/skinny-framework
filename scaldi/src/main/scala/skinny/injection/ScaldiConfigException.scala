package skinny.injection

/**
  * Scaldi configuration exception.
  */
case class ScaldiConfigException(msg: String, e: Throwable) extends Exception(msg, e)
