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

}

