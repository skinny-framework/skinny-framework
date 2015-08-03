package skinny.engine

/**
 * Unstable access validation configuration.
 */
case class UnstableAccessValidation(
  enabled: Boolean,
  createdThreadId: Long = Thread.currentThread.getId)
