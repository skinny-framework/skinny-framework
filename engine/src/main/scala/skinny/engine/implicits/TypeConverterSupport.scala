package skinny.engine.implicits

import scala.language.implicitConversions

import scala.util.control.Exception.allCatch

object TypeConverterSupport extends TypeConverterSupport

/**
 * Support types and implicits for [[TypeConverter]].
 */
trait TypeConverterSupport {

  implicit def safe[S, T](f: S => T): TypeConverter[S, T] = new TypeConverter[S, T] {
    def apply(s: S): Option[T] = allCatch opt f(s)
  }

  /**
   * Implicit convert a `(String) => Option[T]` function into a `TypeConverter[T]`
   */
  implicit def safeOption[S, T](f: S => Option[T]): TypeConverter[S, T] = new TypeConverter[S, T] {
    def apply(v1: S): Option[T] = allCatch.withApply(_ => None)(f(v1))
  }

}