package skinny.engine.implicits

import scala.language.implicitConversions

trait LowPriorityImplicits extends LowestPriorityImplicits {

  implicit val anyToBoolean: TypeConverter[Any, Boolean] = safe {
    case b: Boolean => b
    case "ON" | "TRUE" | "OK" | "1" | "CHECKED" | "YES" | "ENABLE" | "ENABLED" => true
    case n: Number => n != 0
    case _ => false
  }

  implicit val anyToFloat: TypeConverter[Any, Float] = safe {
    case i: Byte => i.toFloat
    case i: Short => i.toFloat
    case i: Int => i.toFloat
    case i: Long => i.toFloat
    case i: Double => i.toFloat
    case i: Float => i
    case i: String => i.toFloat
  }

  implicit val anyToDouble: TypeConverter[Any, Double] = safe {
    case i: Byte => i.toDouble
    case i: Short => i.toDouble
    case i: Int => i.toDouble
    case i: Long => i.toDouble
    case i: Double => i
    case i: Float => i.toDouble
    case i: String => i.toDouble
  }

  implicit val anyToByte: TypeConverter[Any, Byte] = safe {
    case i: Byte => i
    case i: Short => i.toByte
    case i: Int => i.toByte
    case i: Long => i.toByte
    case i: Double => i.toByte
    case i: Float => i.toByte
    case i: String => i.toByte
  }

  implicit val anyToShort: TypeConverter[Any, Short] = safe {
    case i: Byte => i.toShort
    case i: Short => i
    case i: Int => i.toShort
    case i: Long => i.toShort
    case i: Double => i.toShort
    case i: Float => i.toShort
    case i: String => i.toShort
  }

  implicit val anyToInt: TypeConverter[Any, Int] = safe {
    case i: Byte => i.toInt
    case i: Short => i.toInt
    case i: Int => i
    case i: Long => i.toInt
    case i: Double => i.toInt
    case i: Float => i.toInt
    case i: String => i.toInt
  }

  implicit val anyToLong: TypeConverter[Any, Long] = safe {
    case i: Byte => i.toLong
    case i: Short => i.toLong
    case i: Int => i.toLong
    case i: Long => i
    case i: Double => i.toLong
    case i: Float => i.toLong
    case i: String => i.toLong
  }

  implicit val anyToString: TypeConverter[Any, String] = safe(_.toString)

}
