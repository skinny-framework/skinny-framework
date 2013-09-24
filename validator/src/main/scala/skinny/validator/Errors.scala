package skinny.validator

case class Errors(errors: Map[String, Seq[Error]]) {

  def isEmpty: Boolean = errors.isEmpty
  def size: Int = errors.size
  def get(key: String): Seq[Error] = errors.getOrElse(key, Nil)
  def toMap() = errors
}
