package skinny.validator

import scala.annotation.implicitNotFound

/**
 * Type converter
 * @tparam S from
 * @tparam T to
 */
@implicitNotFound(msg = "Cannot find a TypeConverter type class from ${S} to ${T}")
trait TypeConverter[S, T] {

  def apply(s: S): Option[T]

}
