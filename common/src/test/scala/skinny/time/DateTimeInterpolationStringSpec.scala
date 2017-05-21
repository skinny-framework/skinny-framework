package skinny.time

import org.joda.time._
import org.scalatest._

class DateTimeInterpolationStringSpec extends FlatSpec with Matchers with skinny.time.Implicits {

  behavior of "DateTimeInterpolationString"

  it should "have special keywords" in {
    val today         = LocalDate.now
    val todayDateTime = today.toDateTime(new LocalTime(0, 0, 0))

    joda"now" should not equal (null)
    joda"today".toString should equal(todayDateTime.toString)
    joda"tomorrow".toString should equal(todayDateTime.plusDays(1).toString)
    joda"yesterday".toString should equal(todayDateTime.minusDays(1).toString)

    joda"1 year ago".toString.take(18) should equal(DateTime.now.minusYears(1).toString.take(18))
    joda"2 years ago".toString.take(18) should equal(DateTime.now.minusYears(2).toString.take(18))
    joda"1 year later".toString.take(18) should equal(DateTime.now.plusYears(1).toString.take(18))
    joda"2 years later".toString.take(18) should equal(DateTime.now.plusYears(2).toString.take(18))

    joda"1 month ago".toString.take(18) should equal(DateTime.now.minusMonths(1).toString.take(18))
    joda"2 months ago".toString.take(18) should equal(DateTime.now.minusMonths(2).toString.take(18))
    joda"1 month later".toString.take(18) should equal(DateTime.now.plusMonths(1).toString.take(18))
    joda"2 months later".toString.take(18) should equal(DateTime.now.plusMonths(2).toString.take(18))

    joda"1 day ago".toString.take(18) should equal(DateTime.now.minusDays(1).toString.take(18))
    joda"2 days ago".toString.take(18) should equal(DateTime.now.minusDays(2).toString.take(18))
    joda"1 day later".toString.take(18) should equal(DateTime.now.plusDays(1).toString.take(18))
    joda"2 days later".toString.take(18) should equal(DateTime.now.plusDays(2).toString.take(18))

    joda"1 hour ago".toString.take(18) should equal(DateTime.now.minusHours(1).toString.take(18))
    joda"2 hours ago".toString.take(18) should equal(DateTime.now.minusHours(2).toString.take(18))
    joda"1 hour later".toString.take(18) should equal(DateTime.now.plusHours(1).toString.take(18))
    joda"2 hours later".toString.take(18) should equal(DateTime.now.plusHours(2).toString.take(18))

    joda"1 minute ago".toString.take(18) should equal(DateTime.now.minusMinutes(1).toString.take(18))
    joda"2 minutes ago".toString.take(18) should equal(DateTime.now.minusMinutes(2).toString.take(18))
    joda"1 minute later".toString.take(18) should equal(DateTime.now.plusMinutes(1).toString.take(18))
    joda"2 minutes later".toString.take(18) should equal(DateTime.now.plusMinutes(2).toString.take(18))

    joda"1 second ago".toString.take(18) should equal(DateTime.now.minusSeconds(1).toString.take(18))
    joda"2 seconds ago".toString.take(18) should equal(DateTime.now.minusSeconds(2).toString.take(18))
    joda"1 second later".toString.take(18) should equal(DateTime.now.plusSeconds(1).toString.take(18))
    joda"2 seconds later".toString.take(18) should equal(DateTime.now.plusSeconds(2).toString.take(18))

    jodaDate"now".toString should equal(today.toString)
    jodaDate"today".toString should equal(today.toString)
    jodaDate"tomorrow".toString should equal(today.plusDays(1).toString)
    jodaDate"yesterday".toString should equal(today.minusDays(1).toString)

    jodaDate"1 year ago".toString.take(18) should equal(today.minusYears(1).toString.take(18))
    jodaDate"2 years ago".toString.take(18) should equal(today.minusYears(2).toString.take(18))
    jodaDate"1 year later".toString.take(18) should equal(today.plusYears(1).toString.take(18))
    jodaDate"2 years later".toString.take(18) should equal(today.plusYears(2).toString.take(18))

    jodaDate"1 month ago".toString.take(18) should equal(today.minusMonths(1).toString.take(18))
    jodaDate"2 months ago".toString.take(18) should equal(today.minusMonths(2).toString.take(18))
    jodaDate"1 month later".toString.take(18) should equal(today.plusMonths(1).toString.take(18))
    jodaDate"2 months later".toString.take(18) should equal(today.plusMonths(2).toString.take(18))

    jodaDate"1 day ago".toString.take(18) should equal(today.minusDays(1).toString.take(18))
    jodaDate"2 days ago".toString.take(18) should equal(today.minusDays(2).toString.take(18))
    jodaDate"1 day later".toString.take(18) should equal(today.plusDays(1).toString.take(18))
    jodaDate"2 days later".toString.take(18) should equal(today.plusDays(2).toString.take(18))
  }

  it should "work with DateTime" in {
    {
      joda"2014-01-02".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      joda"2014-1-2".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      joda"2014-01-02 03:04:05".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)

      joda"2014/01/02".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      joda"2014/1/2".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      joda"2014/01/02 03:04:05".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)

      val input1 = "2014/01/02"
      val input2 = "2014/01/02 03:04:05"
      joda"$input1".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      joda"$input2".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)
      joda"${2014}/${1}/${2} ${3}:${4}:${5}".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)
    }

    {
      jodaDateTime"2014/01/02".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      jodaDateTime"2014/01/02 03:04:05".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)

      val input1 = "2014/01/02"
      val input2 = "2014/01/02 03:04:05"
      jodaDateTime"$input1".toString should equal(new DateTime(2014, 1, 2, 0, 0, 0).toString)
      jodaDateTime"$input2".toString should equal(new DateTime(2014, 1, 2, 3, 4, 5).toString)
    }
  }

  it should "reject unexpected params" in {
    intercept[IllegalArgumentException] {
      joda"${true}"
    }
    intercept[IllegalArgumentException] {
      joda"${Some(123)}"
    }
    try {
      joda"${Some(123)}"
    } catch {
      case e: IllegalArgumentException =>
        e.getMessage should equal("Some(123) (type: scala.Some) is not allowed. Use String or number value instead.")
    }
  }

  it should "work with LocalDate" in {
    jodaLocalDate"2014/01/02".toString should equal(new LocalDate(2014, 1, 2).toString)
    jodaDate"2014/01/02 03:04:05".toString should equal(new LocalDate(2014, 1, 2).toString)

    val input = "2014/01/02 03:04:05"
    jodaLocalDate"${input}".toString should equal(new LocalDate(2014, 1, 2).toString)
    jodaDate"${input}".toString should equal(new LocalDate(2014, 1, 2).toString)
  }

  it should "work with LocalTime" in {
    jodaLocalTime"03:04:05" should equal(new LocalTime(3, 4, 5))
    jodaTime"2014/01/02 03:04:05" should equal(new LocalTime(3, 4, 5))

    val input = "03:04:05"
    jodaTime"${input}" should equal(new LocalTime(3, 4, 5))
  }

}
