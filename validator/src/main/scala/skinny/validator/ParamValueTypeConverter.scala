package skinny.validator

import scala.annotation.implicitNotFound

/**
 * Type converter
 * @tparam S from
 * @tparam T to
 */
@implicitNotFound(msg = "Cannot find a ParamValueTypeConverter type class from ${S} to ${T}")
trait ParamValueTypeConverter[S, T] {

  def apply(s: S): Option[T]

}
