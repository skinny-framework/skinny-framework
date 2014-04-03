package skinny

import org.joda.time.{ DateTime => JDateTime, LocalDate => JLocalDate, LocalTime => JLocalTime, _ }
import skinny.util.DateTimeUtil

/**
 * Strong parameter type definition.
 */
trait ParamType {

  def unapply(value: Any): Option[Any]
}

/**
 * Strong parameter type definition.
 */
object ParamType {

  def unexpectedTypeError(v: Any, paramType: ParamType): Any = {
    val typeName = Option(v).map(v => s"(type: ${v.getClass.getCanonicalName})").getOrElse("")
    throw new IllegalArgumentException(s"${v} ${typeName} is unexpected")
  }

  def apply(f: PartialFunction[Any, Any]): ParamType = new ParamType {
    def unapply(value: Any): Option[Any] = {
      PartialFunction.condOpt(value)(f).filter(_ != null)
    }
  }

  val Boolean: ParamType = ParamType {
    case null => false
    case v: String => scala.util.Try(v.toBoolean).getOrElse(false)
    case v: Boolean => v
    case v => unexpectedTypeError(v, Boolean)
  }
  val Double: ParamType = ParamType {
    case null => null
    case v: String => v.toDouble
    case v: Double => v
    case v => unexpectedTypeError(v, Double)
  }
  val Float: ParamType = ParamType {
    case null => null
    case v: String => v.toFloat
    case v: Float => v
    case v => unexpectedTypeError(v, Float)
  }
  val Long: ParamType = ParamType {
    case null => null
    case v: String => v.toLong
    case v: Long if v < scala.Long.MinValue => unexpectedTypeError(v, Long)
    case v: Long if v > scala.Long.MaxValue => unexpectedTypeError(v, Long)
    case v: Long => v
    case v: Int => v.toLong
    case v => unexpectedTypeError(v, Long)
  }
  val Int: ParamType = ParamType {
    case null => null
    case v: String => v.toInt
    case v: Int if v < scala.Int.MinValue => unexpectedTypeError(v, Int)
    case v: Int if v > scala.Int.MaxValue => unexpectedTypeError(v, Int)
    case v: Int => v
    case v: Long if v < scala.Int.MinValue => unexpectedTypeError(v, Int)
    case v: Long if v > scala.Int.MaxValue => unexpectedTypeError(v, Int)
    case v: Long => v.toInt
    case v => unexpectedTypeError(v, Int)
  }
  val Short: ParamType = ParamType {
    case null => null
    case v: String => v.toShort
    case v: Short if v < scala.Short.MinValue => unexpectedTypeError(v, Short)
    case v: Short if v > scala.Short.MaxValue => unexpectedTypeError(v, Short)
    case v: Short => v
    case v: Int if v < scala.Short.MinValue => unexpectedTypeError(v, Short)
    case v: Int if v > scala.Short.MaxValue => unexpectedTypeError(v, Short)
    case v: Int => v.toShort
    case v => unexpectedTypeError(v, Short)
  }
  val BigDecimal: ParamType = ParamType {
    case null => null
    case v: String => scala.math.BigDecimal(v)
    case v: scala.math.BigDecimal => v
    case v: Long => scala.math.BigDecimal(v)
    case v: Int => scala.math.BigDecimal(v)
    case v: Double => scala.math.BigDecimal(v)
    case v: Float => scala.math.BigDecimal(v)
    case v: Short => scala.math.BigDecimal(v)
    case v => unexpectedTypeError(v, BigDecimal)
  }
  val String: ParamType = ParamType {
    case null => null
    case v: String => v
    case v => v.toString
  }
  val Byte: ParamType = ParamType {
    case null => null
    case v: String => v.toByte
    case v: Byte => v
    case v => unexpectedTypeError(v, Byte)
  }
  val ByteArray: ParamType = ParamType {
    case null => null
    case v: String => v.getBytes
    case v: Array[Byte] => v
    case v => unexpectedTypeError(v, ByteArray)
  }
  val DateTime: ParamType = ParamType {
    case null => null
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.DateTime))
    case v: JDateTime => v
    case v: java.util.Date => v
    case v => unexpectedTypeError(v, DateTime)
  }
  val LocalDate: ParamType = ParamType {
    case null => null
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalDate)).toLocalDate
    case v: JLocalDate => v
    case v => unexpectedTypeError(v, LocalDate)
  }
  val LocalTime: ParamType = ParamType {
    case null => null
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalTime)).toLocalTime
    case v: JLocalTime => v
    case v => unexpectedTypeError(v, LocalTime)
  }

}
