package org.scalatra.json

import scala.language.implicitConversions

import java.text.{ DateFormat, SimpleDateFormat }
import java.util.Date

import org.json4s._
import org.scalatra.util.conversion._
import org.scalatra.json.JsonConversions._

trait JsonImplicitConversions extends TypeConverterSupport {

  implicit protected def jsonFormats: Formats

  implicit val jsonToBoolean: TypeConverter[JValue, Boolean] = safe(j => j.extractOpt[Boolean] getOrElse j.extract[String].toBoolean)

  implicit val jsonToFloat: TypeConverter[JValue, Float] = safe(j => j.extractOpt[Float] getOrElse j.extract[String].toFloat)

  implicit val jsonToDouble: TypeConverter[JValue, Double] = safe(j => j.extractOpt[Double] getOrElse j.extract[String].toDouble)

  implicit val jsonToByte: TypeConverter[JValue, Byte] = safe(j => j.extractOpt[Byte] getOrElse j.extract[String].toByte)

  implicit val jsonToShort: TypeConverter[JValue, Short] = safe(j => j.extractOpt[Short] getOrElse j.extract[String].toShort)

  implicit val jsonToInt: TypeConverter[JValue, Int] = safe(j => j.extractOpt[Int] getOrElse j.extract[String].toInt)

  implicit val jsonToLong: TypeConverter[JValue, Long] = safe(j => j.extractOpt[Long] getOrElse j.extract[String].toLong)

  implicit val jsonToSelf: TypeConverter[JValue, String] = safe(_.extract[String])

  implicit val jsonToBigInt: TypeConverter[JValue, BigInt] = safeOption(_ match {
    case JInt(bigint) => Some(bigint)
    case JString(v) => Some(BigInt(v))
    case _ => None
  })

  def jsonToDate(format: => String): TypeConverter[JValue, Date] = jsonToDateFormat(new SimpleDateFormat(format))

  def jsonToDateFormat(format: => DateFormat): TypeConverter[JValue, Date] =
    safeOption(_.extractOpt[String] map format.parse)

  implicit def jsonToSeq[T](implicit elementConverter: TypeConverter[JValue, T], mf: Manifest[T]): TypeConverter[JValue, Seq[T]] =
    safe(_.extract[List[T]])

  implicit def jsonToValTypeConversion(source: JValue) = new JsonValConversion(source)

  implicit def jsonToDateConversion(source: JValue) = new JsonDateConversion(source, jsonToDate(_))

  implicit def jsonToSeqConversion(source: JValue) = new {
    def asSeq[T](implicit mf: Manifest[T], tc: TypeConverter[JValue, T]): Option[Seq[T]] = jsonToSeq[T].apply(source)
  }

}
