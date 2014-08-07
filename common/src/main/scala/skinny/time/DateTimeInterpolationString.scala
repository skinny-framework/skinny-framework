package skinny.time

import org.joda.time._
import skinny.util.DateTimeUtil

private[time] object LastParam

/**
 * String interpolation as a factory of joda-time values.
 */
class DateTimeInterpolationString(val s: StringContext) extends AnyVal {

  // DateTime

  def joda(params: Any*): DateTime = jodaDateTime(params: _*)
  def jodaDateTime(params: Any*): DateTime = DateTimeUtil.parseDateTime(buildInterpolatedString(params: _*))

  // LocalDate

  def jodaLocalDate(params: Any*): LocalDate = DateTimeUtil.parseLocalDate(buildInterpolatedString(params: _*))
  def jodaDate(params: Any*): LocalDate = jodaLocalDate(params: _*)

  // LocalTime

  def jodaLocalTime(params: Any*): LocalTime = DateTimeUtil.parseLocalTime(buildInterpolatedString(params: _*))
  def jodaTime(params: Any*): LocalTime = jodaLocalTime(params: _*)

  private def buildInterpolatedString(params: Any*): String = {
    s.parts.zipAll(params, "", LastParam).foldLeft(new StringBuilder) {
      case (sb, (previousQueryPart, LastParam)) => sb ++= previousQueryPart
      case (sb, (previousQueryPart, param)) => sb ++= previousQueryPart ++=
        Option(param).map {
          case s: String => s
          case n: Number => n.toString
          case v => throw new IllegalArgumentException(s"${v} (type: ${v.getClass.getCanonicalName}) is not allowed. Use String or number value instead.")
        }.getOrElse("")
    }.toString
  }

}
