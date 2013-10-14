package skinny.validator.implicits

import scala.language.implicitConversions

import scala.util.control.Exception._
import java.util.Date
import java.text.{ DateFormat, SimpleDateFormat }
import skinny.validator.TypeConverter

/**
 * Implicits to enable using #getAs[Type]("name") for params.
 */
object ParamsGetAsImplicits extends ParamsGetAsImplicits

/**
 * Implicits to enable using #getAs[Type]("name") for params.
 */
trait ParamsGetAsImplicits {

  implicit def skinnyValidatorSafe[S, T](f: S => T): TypeConverter[S, T] = new TypeConverter[S, T] {
    def apply(s: S): Option[T] = allCatch opt f(s)
  }

  implicit def skinnyValidatorSafeOption[S, T](f: S => Option[T]): TypeConverter[S, T] = new TypeConverter[S, T] {
    def apply(v1: S): Option[T] = allCatch.withApply(_ => None)(f(v1))
  }

  def skinnyValidatorStringToDate(format: => String): TypeConverter[String, Date] = {
    skinnyValidatorStringToDateFormat(new SimpleDateFormat(format))
  }

  def skinnyValidatorStringToDateFormat(format: => DateFormat): TypeConverter[String, Date] = {
    skinnyValidatorSafe(format.parse(_))
  }

  def skinnyValidatorStringToSeq[T: Manifest](elementConverter: TypeConverter[String, T], separator: String = ","): TypeConverter[String, Seq[T]] = {
    skinnyValidatorSafe(s => s.split(separator).toSeq.flatMap(e => elementConverter(e.trim)))
  }

  implicit def skinnyValidator_defaultStringToSeq[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[String, Seq[T]] = {
    skinnyValidatorStringToSeq[T](elementConverter)
  }

  implicit def skinnyValidatorSeqHead[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[Seq[String], T] = {
    skinnyValidatorSafeOption(_.headOption.flatMap(elementConverter(_)))
  }

  implicit def skinnyValidatorSeqToSeq[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[Seq[String], Seq[T]] = {
    skinnyValidatorSafe(_.flatMap(elementConverter(_)))
  }

  implicit val skinnyValidatorStringToBoolean: TypeConverter[String, Boolean] = skinnyValidatorSafe { s =>
    s.toUpperCase match {
      case "ON" | "TRUE" | "OK" | "1" | "CHECKED" | "YES" | "ENABLE" | "ENABLED" => true
      case _ => false
    }
  }

  implicit val skinnyValidatorStringToFloat: TypeConverter[String, Float] = skinnyValidatorSafe(_.toFloat)
  implicit val skinnyValidatorStringToDouble: TypeConverter[String, Double] = skinnyValidatorSafe(_.toDouble)
  implicit val skinnyValidatorStringToByte: TypeConverter[String, Byte] = skinnyValidatorSafe(_.toByte)
  implicit val skinnyValidatorStringToShort: TypeConverter[String, Short] = skinnyValidatorSafe(_.toShort)
  implicit val skinnyValidatorStringToInt: TypeConverter[String, Int] = skinnyValidatorSafe(_.toInt)
  implicit val skinnyValidatorStringToLong: TypeConverter[String, Long] = skinnyValidatorSafe(_.toLong)
  implicit val skinnyValidatorStringToSelf: TypeConverter[String, String] = skinnyValidatorSafe(identity)

}
