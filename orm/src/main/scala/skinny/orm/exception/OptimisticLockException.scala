package skinny.orm.exception

case class OptimisticLockException(message: String, cause: Throwable = null)
  extends RuntimeException(message, cause)
