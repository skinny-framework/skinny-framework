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

  def apply(f: PartialFunction[Any, Any]): ParamType = new ParamType {
    def unapply(value: Any): Option[Any] = {
      if (value == null) None else PartialFunction.condOpt(value)(f)
    }
  }

  val Boolean: ParamType = new ParamType {
    def unapply(v: Any): Option[Any] = v match {
      case null => Some(false)
      case v: String => Some(scala.util.Try(v.toBoolean).getOrElse(false))
      case v: Boolean => Some(v)
    }
  }
  val Double: ParamType = ParamType {
    case v: String => v.toDouble
    case v: Double => v
  }
  val Float: ParamType = ParamType {
    case v: String => v.toFloat
    case v: Float => v
  }
  val Long: ParamType = ParamType {
    case v: String => v.toLong
    case v: Long => v
    case v: Int => v.toLong
  }
  val Int: ParamType = ParamType {
    case v: String => v.toInt
    case v: Int => v
    case v: Long if v >= scala.Int.MinValue && v <= scala.Int.MaxValue => v
  }
  val Short: ParamType = ParamType {
    case v: String => v.toShort
    case v: Short => v
    case v: Int if v >= scala.Short.MinValue && v <= scala.Short.MaxValue => v.toShort
    case v: Long if v >= scala.Short.MinValue && v <= scala.Short.MaxValue => v.toShort
  }
  val BigDecimal: ParamType = ParamType {
    case v: String => scala.math.BigDecimal(v)
    case v: scala.math.BigDecimal => v
    case v: Long => scala.math.BigDecimal(v)
    case v: Int => scala.math.BigDecimal(v)
    case v: Double => scala.math.BigDecimal(v)
    case v: Float => scala.math.BigDecimal(v)
    case v: Short => scala.math.BigDecimal(v)
  }
  val String: ParamType = ParamType {
    case v: String => v
    case v => v.toString
  }
  val Byte: ParamType = ParamType {
    case v: String => v.toByte
    case v: Byte => v
  }
  val ByteArray: ParamType = ParamType {
    case v: String => v.getBytes
    case v: Array[Byte] => v
  }
  val DateTime: ParamType = ParamType {
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.DateTime))
    case v: JDateTime => v
    case v: java.util.Date => v
  }
  val LocalDate: ParamType = ParamType {
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalDate)).toLocalDate
    case v: JLocalDate => v
  }
  val LocalTime: ParamType = ParamType {
    case v: String => JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalTime)).toLocalTime
    case v: JLocalTime => v
  }

}
