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
 * Basic template for ParamType implementation.
 */
abstract class AbstractParamType(matcher: PartialFunction[Any, Any]) extends ParamType {

  override def unapply(value: Any): Option[Any] = {
    if (value == null) None else PartialFunction.condOpt(value)(matcher)
  }
}

/**
 * Strong parameter type definition.
 */
object ParamType {

  def apply(f: PartialFunction[Any, Any]): ParamType = new AbstractParamType(f) {}

  case object Boolean extends ParamType {
    def unapply(v: Any): Option[Any] = v match {
      case null => Some(false)
      case v: String => Some(scala.util.Try(v.toBoolean).getOrElse(false))
      case v: Boolean => Some(v)
    }
  }
  case object Double extends AbstractParamType({
    case v: String => v.toDouble
    case v: Double => v
  })
  case object Float extends AbstractParamType({
    case v: String => v.toFloat
    case v: Float => v
  })
  case object Long extends AbstractParamType({
    case v: String => v.toLong
    case v: Long => v
    case v: Int => v.toLong
  })
  case object Int extends AbstractParamType({
    case v: String => v.toInt
    case v: Int => v
    case v: Long if v >= scala.Int.MinValue && v <= scala.Int.MaxValue => v
  })
  case object Short extends AbstractParamType({
    case v: String => v.toShort
    case v: Short => v
    case v: Int if v >= scala.Short.MinValue && v <= scala.Short.MaxValue => v.toShort
    case v: Long if v >= scala.Short.MinValue && v <= scala.Short.MaxValue => v.toShort
  })
  case object BigDecimal extends AbstractParamType({
    case v: String => scala.math.BigDecimal(v)
    case v: scala.math.BigDecimal => v
    case v: Long => scala.math.BigDecimal(v)
    case v: Int => scala.math.BigDecimal(v)
    case v: Double => scala.math.BigDecimal(v)
    case v: Float => scala.math.BigDecimal(v)
    case v: Short => scala.math.BigDecimal(v)
  })
  case object String extends AbstractParamType({
    case v: String => v
    case v => v.toString
  })
  case object Byte extends AbstractParamType({
    case v: String => v.toByte
    case v: Byte => v
  })
  case object ByteArray extends AbstractParamType({
    case v: String => v.getBytes
    case v: Array[Byte] => v
  })
  case object DateTime extends ParamType {
    def unapply(v: Any): Option[Any] = v match {
      case null => None
      case v: String => Some(JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.DateTime)))
      case v: JDateTime => Some(v)
      case v: java.util.Date => Some(v)
    }
  }
  case object LocalDate extends ParamType {
    def unapply(v: Any): Option[Any] = v match {
      case null => None
      case v: String => Some(JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalDate)).toLocalDate)
      case v: JLocalDate => Some(v)
    }
  }
  case object LocalTime extends ParamType {
    def unapply(v: Any): Option[Any] = v match {
      case null => None
      case v: String => Some(JDateTime.parse(DateTimeUtil.toISODateTimeFormat(v, ParamType.LocalTime)).toLocalTime)
      case v: JLocalTime => Some(v)
    }
  }

}
