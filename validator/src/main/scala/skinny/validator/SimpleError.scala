package skinny.validator

/**
 * Simple implementation of Error.
 */
case class SimpleError(override val name: String, override val messageParams: Seq[Any])
  extends Error