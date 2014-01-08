package skinny.validator.implicits

import scala.language.implicitConversions

import scala.util.control.Exception._
import java.util.Date
import java.text.{ DateFormat, SimpleDateFormat }
import skinny.validator.ParamValueTypeConverter

/**
 * Implicits to enable using #getAs[Type]("name") for params.
 */
object ParametersGetAsImplicits extends ParametersGetAsImplicits

/**
 * Implicits to enable using #getAs[Type]("name") for params.
 */
trait ParametersGetAsImplicits {

  implicit def skinnyValidatorSafe[S, T](f: S => T): ParamValueTypeConverter[S, T] = new ParamValueTypeConverter[S, T] {
    def apply(s: S): Option[T] = allCatch opt f(s)
  }

  implicit def skinnyValidatorSafeOption[S, T](f: S => Option[T]): ParamValueTypeConverter[S, T] = new ParamValueTypeConverter[S, T] {
    def apply(v1: S): Option[T] = allCatch.withApply(_ => None)(f(v1))
  }

  def skinnyValidatorStringToDate(format: => String): ParamValueTypeConverter[String, Date] = {
    skinnyValidatorStringToDateFormat(new SimpleDateFormat(format))
  }

  def skinnyValidatorStringToDateFormat(format: => DateFormat): ParamValueTypeConverter[String, Date] = {
    skinnyValidatorSafe(format.parse(_))
  }

  def skinnyValidatorStringToSeq[T: Manifest](elementConverter: ParamValueTypeConverter[String, T], separator: String = ","): ParamValueTypeConverter[String, Seq[T]] = {
    skinnyValidatorSafe(s => s.split(separator).toSeq.flatMap(e => elementConverter(e.trim)))
  }

  implicit def skinnyValidator_defaultStringToSeq[T](implicit elementConverter: ParamValueTypeConverter[String, T], mf: Manifest[T]): ParamValueTypeConverter[String, Seq[T]] = {
    skinnyValidatorStringToSeq[T](elementConverter)
  }

  implicit def skinnyValidatorSeqHead[T](implicit elementConverter: ParamValueTypeConverter[String, T], mf: Manifest[T]): ParamValueTypeConverter[Seq[String], T] = {
    skinnyValidatorSafeOption(_.headOption.flatMap(elementConverter(_)))
  }

  implicit def skinnyValidatorSeqToSeq[T](implicit elementConverter: ParamValueTypeConverter[String, T], mf: Manifest[T]): ParamValueTypeConverter[Seq[String], Seq[T]] = {
    skinnyValidatorSafe(_.flatMap(elementConverter(_)))
  }

  implicit val skinnyValidatorStringToBoolean: ParamValueTypeConverter[String, Boolean] = skinnyValidatorSafe { s =>
    s.toUpperCase match {
      case "ON" | "TRUE" | "OK" | "1" | "CHECKED" | "YES" | "ENABLE" | "ENABLED" => true
      case _ => false
    }
  }

  implicit val skinnyValidatorStringToFloat: ParamValueTypeConverter[String, Float] = skinnyValidatorSafe(_.toFloat)
  implicit val skinnyValidatorStringToDouble: ParamValueTypeConverter[String, Double] = skinnyValidatorSafe(_.toDouble)
  implicit val skinnyValidatorStringToByte: ParamValueTypeConverter[String, Byte] = skinnyValidatorSafe(_.toByte)
  implicit val skinnyValidatorStringToShort: ParamValueTypeConverter[String, Short] = skinnyValidatorSafe(_.toShort)
  implicit val skinnyValidatorStringToInt: ParamValueTypeConverter[String, Int] = skinnyValidatorSafe(_.toInt)
  implicit val skinnyValidatorStringToLong: ParamValueTypeConverter[String, Long] = skinnyValidatorSafe(_.toLong)
  implicit val skinnyValidatorStringToSelf: ParamValueTypeConverter[String, String] = skinnyValidatorSafe(identity)

}
