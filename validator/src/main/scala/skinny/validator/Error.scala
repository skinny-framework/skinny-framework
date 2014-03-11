package skinny.validator

/**
 * Validation error.
 */
trait Error {

  /**
   * Error name.
   */
  def name: String

  /**
   * Params to be embedded to message.
   */
  def messageParams: Seq[Any] = Nil

  override def toString(): String = {
    "Error(name = " + name + ", messageParams = " + messageParams + ")"
  }

  override def equals(other: Any): Boolean = {
    if (other != null && other.isInstanceOf[Error]) {
      val otherError = other.asInstanceOf[Error]
      otherError.name == name && otherError.messageParams == messageParams
    } else false
  }
  override def hashCode() = name.hashCode + messageParams.hashCode

}

/**
 * Factory
 */
object Error {

  def apply(name: String, messageParams: Seq[Any]): Error = new SimpleError(
    name, messageParams
  )

  def unapply(error: Error): Option[(String, Seq[Any])] = {
    Some((error.name, error.messageParams))
  }

}
