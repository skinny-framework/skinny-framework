package skinny.time

import org.joda.time._
import skinny.util.DateTimeUtil
import scala.collection.mutable.WrappedArray

private[time] object LastParam
private[time] sealed trait JodaType
private[time] case object DateTimeType extends JodaType
private[time] case object LocalDateType extends JodaType
private[time] case object LocalTimeType extends JodaType

/**
 * String interpolation as a factory of joda-time values.
 */
class DateTimeInterpolationString(val s: StringContext) extends AnyVal {

  // DateTime

  def joda(params: Any*): DateTime = jodaDateTime(params: _*)
  def jodaDateTime(params: Any*): DateTime = DateTimeUtil.parseDateTime(buildInterpolatedString(params, DateTimeType))

  // LocalDate

  def jodaLocalDate(params: Any*): LocalDate = DateTimeUtil.parseLocalDate(buildInterpolatedString(params, LocalDateType))
  def jodaDate(params: Any*): LocalDate = jodaLocalDate(params: _*)

  // LocalTime

  def jodaLocalTime(params: Any*): LocalTime = DateTimeUtil.parseLocalTime(buildInterpolatedString(params, LocalTimeType))
  def jodaTime(params: Any*): LocalTime = jodaLocalTime(params: _*)

  private def buildInterpolatedString(params: Seq[Any], jodaType: JodaType): String = {
    val str = s.parts.zipAll(params, "", LastParam).foldLeft(new StringBuilder) {
      case (sb, (previousQueryPart, LastParam)) => sb ++= previousQueryPart
      case (sb, (previousQueryPart, param)) => sb ++= previousQueryPart ++=
        Option(param).map {
          case s: String => s
          case n: Number => n.toString
          case v => throw new IllegalArgumentException(s"${v} (type: ${v.getClass.getCanonicalName}) is not allowed. Use String or number value instead.")
        }.getOrElse("")
    }.toString

    def reportUs: Int = throw new Exception("This is a skinny-common's bug. Please report us this issue!")

    (str, jodaType) match {
      case (str, _) if str.matches("""^\d+\s+years?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusYears(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusYears(amount.toInt).toString
        }
      case (str, _) if str.matches("""^\d+\s+months?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusMonths(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusMonths(amount.toInt).toString
        }
      case (str, _) if str.matches("""^\d+\s+days?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusDays(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusDays(amount.toInt).toString
        }
      case (str, _) if str.matches("""^\d+\s+hours?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusHours(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusHours(amount.toInt).toString
        }
      case (str, _) if str.matches("""^\d+\s+minutes?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusMinutes(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusMinutes(amount.toInt).toString
        }
      case (str, _) if str.matches("""^\d+\s+seconds?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago") => DateTime.now.minusSeconds(amount.toInt).toString
          case Array(amount, _, "later") => DateTime.now.plusSeconds(amount.toInt).toString
        }
      case ("now", DateTimeType) => DateTime.now.toString
      case ("now", LocalDateType) => LocalDate.now.toString
      case ("now", LocalTimeType) => LocalTime.now.toString
      case ("today", DateTimeType) => LocalDate.now.toString
      case ("today", LocalDateType) => LocalDate.now.toString
      case ("yesterday", DateTimeType) => LocalDate.now.minusDays(1).toString
      case ("yesterday", LocalDateType) => LocalDate.now.minusDays(1).toString
      case ("tomorrow", DateTimeType) => LocalDate.now.plusDays(1).toString
      case ("tomorrow", LocalDateType) => LocalDate.now.plusDays(1).toString
      case _ => str
    }
  }

}
