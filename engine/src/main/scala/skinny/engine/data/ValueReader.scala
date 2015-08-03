package skinny.engine.data

/**
 * Value reader.
 */
trait ValueReader[S, U] {

  def data: S

  def read(key: String): Either[String, Option[U]]

}
