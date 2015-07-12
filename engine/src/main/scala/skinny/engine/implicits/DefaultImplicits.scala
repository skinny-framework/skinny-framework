package skinny.engine.implicits

import scala.language.implicitConversions

import java.text.{ DateFormat, SimpleDateFormat }
import java.util.Date

/**
 * Implicit TypeConverter values for value types and some factory method for
 * dates and seqs.
 */
trait DefaultImplicits extends LowPriorityImplicits {

  implicit val stringToBoolean: TypeConverter[String, Boolean] = safe { s =>
    s.toUpperCase match {
      case "ON" | "TRUE" | "OK" | "1" | "CHECKED" | "YES" | "ENABLE" | "ENABLED" => true
      case _ => false
    }
  }

  implicit val stringToFloat: TypeConverter[String, Float] = safe(_.toFloat)

  implicit val stringToDouble: TypeConverter[String, Double] = safe(_.toDouble)

  implicit val stringToByte: TypeConverter[String, Byte] = safe(_.toByte)

  implicit val stringToShort: TypeConverter[String, Short] = safe(_.toShort)

  implicit val stringToInt: TypeConverter[String, Int] = safe(_.toInt)

  implicit val stringToLong: TypeConverter[String, Long] = safe(_.toLong)

  implicit val stringToSelf: TypeConverter[String, String] = safe(identity)

  def stringToDate(format: => String): TypeConverter[String, Date] = stringToDateFormat(new SimpleDateFormat(format))

  def stringToDateFormat(format: => DateFormat): TypeConverter[String, Date] = safe(format.parse(_))

  implicit def defaultStringToSeq[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[String, Seq[T]] =
    stringToSeq[T](elementConverter)

  def stringToSeq[T: Manifest](elementConverter: TypeConverter[String, T], separator: String = ","): TypeConverter[String, Seq[T]] =
    safe(s => s.split(separator).toSeq.flatMap(e => elementConverter(e.trim)))

  implicit def seqHead[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[Seq[String], T] =
    safeOption(_.headOption.flatMap(elementConverter(_)))

  implicit def seqToSeq[T](implicit elementConverter: TypeConverter[String, T], mf: Manifest[T]): TypeConverter[Seq[String], Seq[T]] =
    safe(_.flatMap(elementConverter(_)))

}

