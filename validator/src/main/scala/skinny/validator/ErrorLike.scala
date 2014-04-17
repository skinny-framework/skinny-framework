package skinny.validator

/**
 * Validation error.
 */
trait ErrorLike {

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
    if (other != null && other.isInstanceOf[ErrorLike]) {
      val otherError = other.asInstanceOf[ErrorLike]
      otherError.name == name && otherError.messageParams == messageParams
    } else false
  }
  override def hashCode() = name.hashCode + messageParams.hashCode

}
