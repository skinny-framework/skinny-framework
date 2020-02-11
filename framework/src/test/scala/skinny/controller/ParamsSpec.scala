package skinny.controller

import org.scalatra.test.scalatest.ScalatraFlatSpec
import skinny._

class ParamsSpec extends ScalatraFlatSpec {

  behavior of "Params"

  object ParasController extends SkinnyController with Routes {
    def date = {
      Params(params)
        .withDate(
          ("year", "month", "day"),
          "date"
        )
        .getAs[String]("date")
        .orNull
    }
    def date2 = {
      Params(params).withDate("date").getAs[String]("date").orNull
    }
    def datetime = {
      Params(params)
        .withDateTime(
          ("year", "month", "day", "hour", "minute", "second"),
          "datetime"
        )
        .getAs[String]("datetime")
        .orNull
    }
    def datetime2 = {
      Params(params).withDateTime("datetime").getAs[String]("datetime").orNull
    }
    def datetime3 = {
      Params(params)
        .withDateTime(
          ("date", "time"),
          "datetime"
        )
        .getAs[String]("datetime")
        .orNull
    }
    def time = {
      Params(params)
        .withTime(
          ("hour", "minute", "second"),
          "time"
        )
        .getAs[String]("time")
        .orNull
    }
    def time2 = {
      Params(params).withTime("time").getAs[String]("time").orNull
    }

    post("/date")(date).as("date")
    post("/date2")(date2).as("date2")
    post("/datetime")(datetime).as("datetime")
    post("/datetime2")(datetime2).as("datetime2")
    post("/datetime3")(datetime3).as("datetime3")
    post("/time")(time).as("time")
    post("/time2")(time2).as("time2")
  }

  addFilter(ParasController, "/*")

  "params" should "should be converted easily" in {
    post("/date", "year" -> "2011", "month" -> "6", "day" -> "22") {
      body should equal("2011-06-22")
    }
    post("/date2", "date_year" -> "2011", "date_month" -> "6", "date_day" -> "22") {
      body should equal("2011-06-22")
    }
    post("/datetime",
         "year"   -> "2011",
         "month"  -> "6",
         "day"    -> "22",
         "hour"   -> "12",
         "minute" -> "34",
         "second" -> "56") {
      body should equal("2011-06-22 12:34:56")
    }
    post("/datetime2",
         "datetime_year"   -> "2011",
         "datetime_month"  -> "6",
         "datetime_day"    -> "22",
         "datetime_hour"   -> "12",
         "datetime_minute" -> "34",
         "datetime_second" -> "56") {
      body should equal("2011-06-22 12:34:56")
    }
    post("/datetime3", "date" -> "2011-06-22", "time" -> "12:34:56") {
      body should equal("2011-06-22 12:34:56")
    }
    post("/datetime2", "datetime_date" -> "2011-06-22", "datetime_time" -> "12:34:56") {
      body should equal("2011-06-22 12:34:56")
    }
    post("/time", "hour" -> "1", "minute" -> "23", "second" -> "4") {
      body should equal("1970-01-01 01:23:04")
    }
    post("/time2", "time_hour" -> "1", "time_minute" -> "23", "time_second" -> "4") {
      body should equal("1970-01-01 01:23:04")
    }
  }

}
