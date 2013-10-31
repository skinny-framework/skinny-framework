package skinny.orm.feature

import org.joda.time._
import skinny.ParamType

/**
 * Strong parameters support.
 */
trait StrongParametersFeature {

  /**
   * Returns typed value from a strong parameter.
   *
   * @param fieldName field name
   * @param value actual value
   * @param paramType param type definition
   * @return typed value if exists
   */
  protected def getTypedValueFromStrongParameter(fieldName: String, value: Any, paramType: ParamType): Option[Any] = {
    Option(value).map { value =>
      value match {
        case Some(v) => getTypedValueFromStrongParameter(fieldName, v, paramType)
        case None => null
        case v: Boolean if paramType == ParamType.Boolean => v
        case v: Double if paramType == ParamType.Double => v
        case v: Float if paramType == ParamType.Float => v
        case v: Int if paramType == ParamType.Int => v
        case v: Long if paramType == ParamType.Long => v
        case v: Short if paramType == ParamType.Short => v
        case v: Byte if paramType == ParamType.Byte => v
        case v: Array[Byte] if paramType == ParamType.ByteArray => v
        case v: java.util.Date if paramType == ParamType.DateTime => v
        case v: DateTime if paramType == ParamType.DateTime => v
        case v: LocalDate if paramType == ParamType.LocalDate => v
        case v: LocalTime if paramType == ParamType.LocalTime => v
        case v: String if v == "" => null
        case v: String =>
          paramType match {
            case ParamType.Boolean => v.toBoolean
            case ParamType.Byte => v.toByte
            case ParamType.Double => v.toDouble
            case ParamType.Float => v.toFloat
            case ParamType.Int => v.toInt
            case ParamType.Long => v.toLong
            case ParamType.Short => v.toShort
            case ParamType.String => v
            case ParamType.ByteArray => v.getBytes
            case ParamType.DateTime => DateTime.parse(toISODateTimeFormat(v, ParamType.DateTime))
            case ParamType.LocalDate => LocalDate.parse(toISODateTimeFormat(v, ParamType.LocalDate))
            case ParamType.LocalTime => LocalTime.parse(toISODateTimeFormat(v, ParamType.LocalTime))
            case v => v
          }
        case v => throw new IllegalArgumentException(s"Cannot convert '${v}' to ${paramType} value.")
      }
    }
  }

  /**
   * The ISO8601 standard date format.
   */
  private[this] def ISO_DATE_FORMAT = "%04d-%02d-%02dT%02d:%02d:%02d%s"

  /**
   * Returns current timezone value (e.g. +09:00).
   */
  private[this] def currentTimeZone = {
    val minutes = java.util.TimeZone.getDefault.getRawOffset / 1000 / 60
    (if (minutes >= 0) "+" else "-") + "%02d:%02d".format((math.abs(minutes) / 60), (math.abs(minutes) % 60))
  }

  /**
   * Converts string value to ISO8601 date format if possible.
   * @param s string value
   * @param paramType DateTime/LocalDate/LocalTime
   * @return ISO8601 data format string value
   */
  private[this] def toISODateTimeFormat(s: String, paramType: ParamType): String = {
    "(\\d+)".r.findAllIn(s).toList match {
      case year :: month :: day :: hour :: minute :: second :: zoneHour :: zoneMinute :: _ =>
        val timeZone = "([+-]\\d{2}:\\d{2})".r.findFirstIn(s).getOrElse(currentTimeZone)
        ISO_DATE_FORMAT.format(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt, timeZone)
      case year :: month :: day :: hour :: minute :: second :: _ =>
        ISO_DATE_FORMAT.format(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt, currentTimeZone)
      case year :: month :: day :: _ if paramType == ParamType.LocalDate =>
        ISO_DATE_FORMAT.format(year.toInt, month.toInt, day.toInt, 0, 0, 0, currentTimeZone)
      case hour :: minute :: second :: _ if paramType == ParamType.LocalTime =>
        ISO_DATE_FORMAT.format(1970, 1, 1, hour.toInt, minute.toInt, second.toInt, currentTimeZone)
      case _ => s
    }
  }

}
