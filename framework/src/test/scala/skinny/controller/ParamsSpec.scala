package skinny.controller

import org.scalatra.test.scalatest._
import skinny._, controller._

class ParamsSpec extends ScalatraFlatSpec {

  behavior of "Params"

  object ParasController extends SkinnyController with Routes {
    def date = {
      Params(params).withDateValue(
        ("year", "month", "day"), "date").getAs[String]("date").orNull
    }
    def datetime = {
      Params(params).withDateTimeValue(
        ("year", "month", "day", "hour", "minute", "second"), "datetime").getAs[String]("datetime").orNull
    }
    def time = {
      Params(params).withTimeValue(
        ("hour", "minute", "second"), "time").getAs[String]("time").orNull
    }

    post("/date")(date).as('date)
    post("/datetime")(datetime).as('datetime)
    post("/time")(time).as('time)
  }

  addFilter(ParasController, "/*")

  "params" should "should be converted easily" in {
    post("/date", "year" -> "2011", "month" -> "6", "day" -> "22") {
      body should equal("2011-06-22")
    }
    post("/datetime", "year" -> "2011", "month" -> "6", "day" -> "22", "hour" -> "12", "minute" -> "34", "second" -> "56") {
      body should equal("2011-06-22 12:34:56")
    }
    post("/time", "hour" -> "1", "minute" -> "23", "second" -> "4") {
      body should equal("1970-01-01 01:23:04")
    }
  }

}
