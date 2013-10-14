package skinny.validator

/**
 * Collection of errors.
 *
 * @param errors Map value.
 */
case class Errors(errors: Map[String, Seq[Error]]) {

  /**
   * No error if true.
   *
   * @return true/false
   */
  def isEmpty: Boolean = errors.isEmpty

  /**
   * Returns size of errors.
   *
   * @return size
   */
  def size: Int = errors.size

  /**
   * Returns errors for the key.
   *
   * @param key key
   * @return errors for the key
   */
  def get(key: String): Seq[Error] = errors.getOrElse(key, Nil)

  /**
   * Returns underlying Map value.
   *
   * @return Map value
   */
  def toMap() = errors

}
