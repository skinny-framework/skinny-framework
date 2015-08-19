package skinny.engine.implicits

import scala.annotation.implicitNotFound

/**
 * A type class to convert values.
 */
@implicitNotFound(msg = "Cannot find a TypeConverter type class from ${S} to ${T}")
trait TypeConverter[S, T] {

  def apply(s: S): Option[T]

}
