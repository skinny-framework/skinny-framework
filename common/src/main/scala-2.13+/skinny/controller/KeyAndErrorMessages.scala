package skinny.controller

/**
  * Key and error messages from input validation.
  *
  * @see ValidationFeature
  */
case class KeyAndErrorMessages(underlying: Map[String, Seq[String]] = Map[String, Seq[String]]())
    extends Map[String, Seq[String]] {

  override def +[B1 >: Seq[String]](kv: (String, B1)): Map[String, B1] = underlying + kv
  override def iterator: Iterator[(String, Seq[String])]               = underlying.iterator
  override def get(key: String): Option[Seq[String]]                   = underlying.get(key)

  def hasErrors(key: String): Boolean = underlying.get(key).map(_.size > 0).getOrElse(false)

  def getErrors(key: String): Seq[String] = get(key).getOrElse(Nil)

  def remove(key: String) = underlying - key

  def updated[V1 >: Seq[String]](key: String, value: V1) = underlying + (key -> value)

}
