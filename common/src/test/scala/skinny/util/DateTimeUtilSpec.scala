package skinny.util

import org.joda.time.{ DateTimeZone, LocalTime }
import org.scalatest._
import skinny.ParamType

class DateTimeUtilSpec extends FlatSpec with Matchers {

  behavior of "DateTimeUtil"

  it should "have #currentTimeZone" in {
    DateTimeUtil.currentTimeZone should not be (null)
  }

  it should "have #parseDateTime" in {
    val dt = DateTimeUtil.parseDateTime("2013-02-03 12:34:56")
    dt.getYearOfEra should equal(2013)
    dt.getMonthOfYear should equal(2)
    dt.getDayOfMonth should equal(3)
    dt.getHourOfDay should equal(12)
    dt.getMinuteOfHour should equal(34)
    dt.getSecondOfMinute should equal(56)
  }

  it should "have #parseLocalDate" in {
    val ld = DateTimeUtil.parseLocalDate("2013-02-03")
    ld.getYearOfEra should equal(2013)
    ld.getMonthOfYear should equal(2)
    ld.getDayOfMonth should equal(3)
  }

  it should "have #parseLocalTime" in {
    val lt = DateTimeUtil.parseLocalTime("12:34:56")
    lt.getHourOfDay should equal(12)
    lt.getMinuteOfHour should equal(34)
    lt.getSecondOfMinute should equal(56)
  }

  it should "have #toISODateTimeFormat" in {
    val zone = DateTimeUtil.currentTimeZone
    DateTimeUtil.toISODateTimeFormat("", ParamType.DateTime) should equal("")
    DateTimeUtil.toISODateTimeFormat("2013-01-02 03:04:05", ParamType.DateTime) should equal(s"2013-01-02T03:04:05${zone}")
    DateTimeUtil.toISODateTimeFormat("2013-01-2a 03:04:05", ParamType.DateTime) should equal(s"2013-01-2aT03:04:05${zone}")
    DateTimeUtil.toISODateTimeFormat("2013-01-02T03:04:05", ParamType.DateTime) should equal(s"2013-01-02T03:04:05${zone}")
    DateTimeUtil.toISODateTimeFormat("2013-01-02T03:04:05+09:00", ParamType.DateTime) should equal("2013-01-02T03:04:05+09:00")
    DateTimeUtil.toISODateTimeFormat("2014-03-09T16:42:33.816+09:00", ParamType.DateTime) should equal("2014-03-09T16:42:33.816+09:00")
  }

  it should "have #toString" in {
    val dt = DateTimeUtil.parseDateTime("2013-02-03 15:11:22+00:00")
    DateTimeUtil.toString(dt) should equal("2013-02-03 15:11:22")

    val t = new LocalTime(dt.toInstant, DateTimeZone.UTC)
    DateTimeUtil.toString(t) should equal("15:11:22")
  }

  it should "have #nowString" in {
    DateTimeUtil.nowString should not equal (null)
  }

  it should "have #toDateString" in {
    DateTimeUtil.toDateString(Map("year" -> 2013, "month" -> 12, "day" -> 23)) should equal(Some("2013-12-23"))
    DateTimeUtil.toUnsafeDateString(Map("year" -> 2013, "month" -> 12, "day" -> 23)) should equal(Some("2013-12-23"))
  }

  it should "have #toTimeString" in {
    DateTimeUtil.toTimeString(Map("hour" -> 1, "minute" -> 2, "second" -> 3)) should equal(Some("1970-01-01 01:02:03"))
    DateTimeUtil.toUnsafeTimeString(Map("hour" -> 1, "minute" -> 2, "second" -> 3)) should equal(Some("1970-01-01 01:02:03"))
  }

  it should "have #toDateTimeString" in {
    DateTimeUtil.toDateTimeString(
      Map("year" -> 2013, "month" -> 12, "day" -> 23, "hour" -> 1, "minute" -> 2, "second" -> 3)
    ) should equal(Some("2013-12-23 01:02:03"))
    DateTimeUtil.toUnsafeDateTimeString(
      Map("year" -> 2013, "month" -> 12, "day" -> 23, "hour" -> 1, "minute" -> 2, "second" -> 3)
    ) should equal(Some("2013-12-23 01:02:03"))
  }

  it should "have #toUnsafeDateTimeStringFromDateAndTime" in {
    DateTimeUtil.toUnsafeDateTimeStringFromDateAndTime(
      Map("date" -> "2013-01-02", "time" -> "01:02:03")) should equal(Some("2013-01-02 01:02:03"))
  }

  it should "have #isLocalDateFormat(String)" in {
    DateTimeUtil.isLocalDateFormat("foo") should equal(false)
    DateTimeUtil.isLocalDateFormat("2013-04-05") should equal(true)
  }

  it should "have #isDateTimeFormat(String)" in {
    DateTimeUtil.isDateTimeFormat("foo") should equal(false)
    DateTimeUtil.isDateTimeFormat("2013-04-05 01:02:03") should equal(true)
  }

}
