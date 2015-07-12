package skinny.engine.multipart

class SizeConstraintExceededException(
  message: String,
  t: Throwable)
    extends Exception(message, t)

