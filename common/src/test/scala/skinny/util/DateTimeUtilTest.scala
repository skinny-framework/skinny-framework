package skinny.util

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.ParamType

class DateTimeUtilTest extends FlatSpec with ShouldMatchers {

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
  }

}
