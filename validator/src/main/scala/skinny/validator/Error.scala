package skinny.validator

/**
 * Simple implementation of Error.
 */
case class Error(
  override val name: String,
  override val messageParams: Seq[Any]
) extends ErrorLike

/**
 * Factory
 */
object Error {

  def unapply(error: ErrorLike): Option[(String, Seq[Any])] = {
    Some((error.name, error.messageParams))
  }

}
