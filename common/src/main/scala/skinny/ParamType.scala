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
    def unapply(value: Any): Option[Any] = PartialFunction.condOpt(value)(f)
  }

  val Boolean = ParamType {
    case v: String => v.toBoolean
    case v: Boolean => v
  }
  val Double = ParamType {
    case v: String => v.toDouble
    case v: Double => v
  }
  val Float = ParamType {
    case v: String => v.toFloat
    case v: Float => v
  }
  val Long = ParamType {
    case v: String => v.toLong
    case v: Long => v
  }
  val Int = ParamType {
    case v: String => v.toInt
    case v: Int => v
  }
  val Short = ParamType {
    case v: String => v.toShort
    case v: Short => v
  }
  val BigDecimal = ParamType {
    case v: String => scala.math.BigDecimal(v)
    case v: scala.math.BigDecimal => v
  }
  val String = ParamType {
    case v: String => v
  }
  val Byte = ParamType {
    case v: String => v.toByte
    case v: Byte => v
  }
  val ByteArray = ParamType {
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
