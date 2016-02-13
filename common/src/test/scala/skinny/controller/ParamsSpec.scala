package skinny.controller

import org.scalatest._
import skinny.ParamType

class ParamsSpec extends FlatSpec with Matchers {

  behavior of "Params"

  it should "have constants" in {
    Params.Year should equal("Year")
    Params.Month should equal("Month")
    Params.Day should equal("Day")
    Params.Hour should equal("Hour")
    Params.Minute should equal("Minute")
    Params.Second should equal("Second")
    Params.Date should equal("Date")
    Params.Time should equal("Time")

    Params._year should equal("_year")
    Params._month should equal("_month")
    Params._day should equal("_day")
    Params._hour should equal("_hour")
    Params._minute should equal("_minute")
    Params._second should equal("_second")
    Params._date should equal("_date")
    Params._time should equal("_time")
  }

  it should "work with nullable values" in {
    // Some(null) must be avoided
    {
      val params = Params(Map("something" -> "foo"))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some("foo"))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> null))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some("foo")))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some("foo"))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some(Some("foo"))))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(Some(Some("foo")))

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> Some(null)))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
    {
      val params = Params(Map("something" -> None))

      params.something.exists(_ == null) should equal(false)
      params.something should equal(None)

      params.other.exists(_ == null) should equal(false)
      params.other.isDefined should equal(false)
    }
  }

  it should "have #permit" in {
    Params(Map("foo" -> "bar", "baz" -> "XXX")).permit("foo" -> ParamType.String).params.size should equal(1)
  }

  it should "have #isSnakeCasedParams" in {
    Params(Map("fooYear" -> 2014)).isSnakeCasedParams("foo") should equal(false)
    Params(Map("foo_year" -> 2014)).isSnakeCasedParams("foo") should equal(true)
  }

  it should "have #toYmdKeys" in {
    Params(Map()).toYmdKeys("foo") should equal(("foo_year", "foo_month", "foo_day"))
  }

  it should "have #toYmdhmsKeys" in {
    Params(Map()).toYmdhmsKeys("foo") should equal(("foo_year", "foo_month", "foo_day", "foo_hour", "foo_minute", "foo_second"))
  }

  it should "have #toHmsKeys" in {
    Params(Map()).toHmsKeys("foo") should equal(("foo_hour", "foo_minute", "foo_second"))
  }

  it should "have #withDate" in {
    val params = Params(Map(
      "foo_year" -> 2013, "foo_month" -> 2, "foo_day" -> 3
    )).withDate("foo")
    params.getAs[String]("foo") should equal(Some("2013-02-03"))
  }

  it should "have #withTime" in {
    val params = Params(Map(
      "foo_hour" -> 4, "foo_minute" -> 5, "foo_second" -> 6
    )).withTime("foo")
    params.getAs[String]("foo") should equal(Some("1970-01-01 04:05:06"))
  }

  it should "have #withDateTime" in {
    val params = Params(Map(
      "foo_year" -> 2013, "foo_month" -> 2, "foo_day" -> 3,
      "foo_hour" -> 4, "foo_minute" -> 5, "foo_second" -> 6
    )).withDateTime("foo")
    params.getAs[String]("foo") should equal(Some("2013-02-03 04:05:06"))
  }

}
