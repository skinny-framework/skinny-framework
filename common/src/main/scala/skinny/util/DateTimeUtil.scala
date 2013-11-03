package skinny.util

import skinny.ParamType
import org.joda.time._

/**
 * DateTime utility.
 */
object DateTimeUtil {

  /**
   * The ISO8601 standard date format.
   */
  val ISO_DATE_FORMAT = "%04d-%02d-%02dT%02d:%02d:%02d%s"

  /**
   * Returns current timezone value (e.g. +09:00).
   */
  def currentTimeZone = {
    val minutes = java.util.TimeZone.getDefault.getRawOffset / 1000 / 60
    (if (minutes >= 0) "+" else "-") + "%02d:%02d".format((math.abs(minutes) / 60), (math.abs(minutes) % 60))
  }

  /**
   * Converts string value to ISO8601 date format if possible.
   * @param s string value
   * @param paramType DateTime/LocalDate/LocalTime
   * @return ISO8601 data format string value
   */
  def toISODateTimeFormat(s: String, paramType: ParamType): String = {
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

  def parseDateTime(s: String): DateTime = DateTime.parse(toISODateTimeFormat(s, ParamType.DateTime))

  def parseLocalDate(s: String): LocalDate = LocalDate.parse(toISODateTimeFormat(s, ParamType.LocalDate))

  def parseLocalTime(s: String): LocalTime = LocalTime.parse(toISODateTimeFormat(s, ParamType.LocalTime))

  def toDateString(
    params: Map[String, Any],
    year: String = "year",
    month: String = "month",
    day: String = "day"): String = {

    "%04d-%02d-%02d".format(
      params.get(year).map(_.toString.toInt).orNull,
      params.get(month).map(_.toString.toInt).orNull,
      params.get(day).map(_.toString.toInt).orNull
    )
  }

  def toTimeString(
    params: Map[String, Any],
    hour: String = "hour",
    minute: String = "minute",
    second: String = "second"): String = {

    "1970-01-01 %02d:%02d:%02d".format(
      params.get(hour).map(_.toString.toInt).orNull,
      params.get(minute).map(_.toString.toInt).orNull,
      params.get(second).map(_.toString.toInt).orNull
    )
  }

  def toDateTimeString(
    params: Map[String, Any],
    year: String = "year",
    month: String = "month",
    day: String = "day",
    hour: String = "hour",
    minute: String = "minute",
    second: String = "second"): String = {

    "%04d-%02d-%02d %02d:%02d:%02d".format(
      params.get(year).map(_.toString.toInt).orNull,
      params.get(month).map(_.toString.toInt).orNull,
      params.get(day).map(_.toString.toInt).orNull,
      params.get(hour).map(_.toString.toInt).orNull,
      params.get(minute).map(_.toString.toInt).orNull,
      params.get(second).map(_.toString.toInt).orNull
    )
  }

}
