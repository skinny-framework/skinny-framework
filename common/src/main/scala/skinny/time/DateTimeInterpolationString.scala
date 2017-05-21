package skinny.time

import org.joda.time._
import skinny.util.DateTimeUtil

private[time] object LastParam
private[time] sealed trait JodaType
private[time] case object DateTimeType  extends JodaType
private[time] case object LocalDateType extends JodaType
private[time] case object LocalTimeType extends JodaType

/**
  * String interpolation as a factory of joda-time values.
  */
class DateTimeInterpolationString(val s: StringContext) extends AnyVal {

  // DateTime

  def joda(params: Any*): DateTime         = jodaDateTime(params: _*)
  def jodaDateTime(params: Any*): DateTime = DateTimeUtil.parseDateTime(buildInterpolatedString(params, DateTimeType))

  // LocalDate

  def jodaLocalDate(params: Any*): LocalDate =
    DateTimeUtil.parseLocalDate(buildInterpolatedString(params, LocalDateType))
  def jodaDate(params: Any*): LocalDate = jodaLocalDate(params: _*)

  // LocalTime

  def jodaLocalTime(params: Any*): LocalTime =
    DateTimeUtil.parseLocalTime(buildInterpolatedString(params, LocalTimeType))
  def jodaTime(params: Any*): LocalTime = jodaLocalTime(params: _*)

  private def string(d: DateTime)  = DateTimeUtil.toString(d)
  private def string(d: LocalDate) = DateTimeUtil.toString(d)
  private def string(d: LocalTime) = DateTimeUtil.toString(d)

  private def buildInterpolatedString(params: Seq[Any], jodaType: JodaType): String = {
    val str = s.parts
      .zipAll(params, "", LastParam)
      .foldLeft(new StringBuilder) {
        case (sb, (previousQueryPart, LastParam)) => sb ++= previousQueryPart
        case (sb, (previousQueryPart, param)) =>
          sb ++= previousQueryPart ++=
            Option(param)
              .map {
                case s: String => s
                case n: Number => n.toString
                case v =>
                  throw new IllegalArgumentException(
                    s"${v} (type: ${v.getClass.getCanonicalName}) is not allowed. Use String or number value instead."
                  )
              }
              .getOrElse("")
      }
      .toString

    def reportUs: Int = throw new Exception("This is a skinny-common's bug. Please report us this issue!")

    (str, jodaType) match {
      case (str, _) if str.matches("""^\d+\s+years?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusYears(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusYears(amount.toInt))
        }
      case (str, _) if str.matches("""^\d+\s+months?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusMonths(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusMonths(amount.toInt))
        }
      case (str, _) if str.matches("""^\d+\s+days?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusDays(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusDays(amount.toInt))
        }
      case (str, _) if str.matches("""^\d+\s+hours?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusHours(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusHours(amount.toInt))
        }
      case (str, _) if str.matches("""^\d+\s+minutes?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusMinutes(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusMinutes(amount.toInt))
        }
      case (str, _) if str.matches("""^\d+\s+seconds?\s+(ago|later)$""") =>
        str.split("\\s+") match {
          case Array(amount, _, "ago")   => string(DateTime.now.minusSeconds(amount.toInt))
          case Array(amount, _, "later") => string(DateTime.now.plusSeconds(amount.toInt))
        }
      case ("now", DateTimeType)        => string(DateTime.now)
      case ("now", LocalDateType)       => string(LocalDate.now)
      case ("now", LocalTimeType)       => string(LocalTime.now)
      case ("today", DateTimeType)      => string(LocalDate.now)
      case ("today", LocalDateType)     => string(LocalDate.now)
      case ("yesterday", DateTimeType)  => string(LocalDate.now.minusDays(1))
      case ("yesterday", LocalDateType) => string(LocalDate.now.minusDays(1))
      case ("tomorrow", DateTimeType)   => string(LocalDate.now.plusDays(1))
      case ("tomorrow", LocalDateType)  => string(LocalDate.now.plusDays(1))
      case _                            => str
    }
  }

}
