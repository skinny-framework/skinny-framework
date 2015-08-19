package skinny.engine.implicits

/**
 * Type extractor.
 */
trait TypeExtractor[T] {

  def converter: TypeConverter[String, T]

  def unapply(source: String): Option[T] = converter(source)

}
