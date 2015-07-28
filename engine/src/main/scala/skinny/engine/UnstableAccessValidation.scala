package skinny.engine

case class UnstableAccessValidation(
  enabled: Boolean,
  createdThreadId: Long = Thread.currentThread.getId)
