package skinny.time

import org.joda.time._
import org.scalatest._

class DateTimeInterpolationStringSpec extends FlatSpec with Matchers
    with skinny.time.Implicits {

  behavior of "DateTimeInterpolationString"

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
