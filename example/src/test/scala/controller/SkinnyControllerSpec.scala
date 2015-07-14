package controller

import org.scalatest._
import skinny.controller.{ SkinnyController, Params }
import skinny.controller.feature.RequestScopeFeature._
import skinny.test._
import org.joda.time._

class SkinnyControllerSpec extends FunSpec with Matchers {

  describe("SkinnyController") {

    describe("setAsParams") {

      case class TestModel(
        text: String,
        textOpt: Option[String],
        number: Long,
        numberOpt: Option[Long],
        nullValue: Object,
        nullValueOpt: Option[Object],
        dateTime: DateTime,
        dateTimeOpt: Option[DateTime],
        localDate: LocalDate,
        localDateOpt: Option[LocalDate],
        localTime: LocalTime,
        localTimeOpt: Option[LocalTime])

      val datetime = DateTime.parse("2014-08-23T13:08:27")
      val datetime2 = DateTime.parse("1990-11-03T04:44:00")
      val localDate = LocalDate.parse("2004-02-29")
      val localDate2 = LocalDate.parse("1970-01-01")
      val localTime = LocalTime.parse("14:59:01")
      val localTime2 = LocalTime.parse("04:00:59")

      val model = TestModel(
        "Text Contents", Option("Option Text"),
        987L, Option(-87654321L),
        null, Some(null),
        datetime, Option(datetime2),
        localDate, Option(localDate2),
        localTime, Option(localTime2)
      )

      it("should set params for model") {
        val ctrl = new SkinnyController with MockController
        ctrl.setAsParams(model)
        implicit val context = ctrl.context

        val params = ctrl.getFromRequestScope[Params](ATTR_PARAMS).get

        params.getAs("text") should be(Some("Text Contents"))
        params.getAs("textOpt") should be(Some("Option Text"))
        params.getAs("number") should be(Some(987L))
        params.getAs("numberOpt") should be(Some(-87654321L))
        params.getAs("nullValue") should be(None)
        params.getAs("nullValueOpt") should be(None)

        params.getAs("dateTime") should be(Some(datetime))
        params.getAs("dateTimeDate") should be(Some("2014-08-23"))
        params.getAs("dateTimeTime") should be(Some("13:08:27"))
        params.getAs("dateTimeYear") should be(Some(2014))
        params.getAs("dateTimeMonth") should be(Some(8))
        params.getAs("dateTimeDay") should be(Some(23))
        params.getAs("dateTimeHour") should be(Some(13))
        params.getAs("dateTimeMinute") should be(Some(8))
        params.getAs("dateTimeSecond") should be(Some(27))

        params.getAs("dateTimeOpt") should be(Some(datetime2))
        params.getAs("dateTimeOptDate") should be(Some("1990-11-03"))
        params.getAs("dateTimeOptTime") should be(Some("04:44:00"))
        params.getAs("dateTimeOptYear") should be(Some(1990))
        params.getAs("dateTimeOptMonth") should be(Some(11))
        params.getAs("dateTimeOptDay") should be(Some(3))
        params.getAs("dateTimeOptHour") should be(Some(4))
        params.getAs("dateTimeOptMinute") should be(Some(44))
        params.getAs("dateTimeOptSecond") should be(Some(0))

        params.getAs("localDate") should be(Some(localDate))
        params.getAs("localDateYear") should be(Some(2004))
        params.getAs("localDateMonth") should be(Some(2))
        params.getAs("localDateDay") should be(Some(29))

        params.getAs("localDateOpt") should be(Some(localDate2))
        params.getAs("localDateOptYear") should be(Some(1970))
        params.getAs("localDateOptMonth") should be(Some(1))
        params.getAs("localDateOptDay") should be(Some(1))

        params.getAs("localTime") should be(Some(localTime))
        params.getAs("localTimeHour") should be(Some(14))
        params.getAs("localTimeMinute") should be(Some(59))
        params.getAs("localTimeSecond") should be(Some(1))

        params.getAs("localTimeOpt") should be(Some(localTime2))
        params.getAs("localTimeOptHour") should be(Some(4))
        params.getAs("localTimeOptMinute") should be(Some(0))
        params.getAs("localTimeOptSecond") should be(Some(59))
      }

      it("should set params for model with useSnakeCasedParamKeys") {
        val ctrl = new SkinnyController with MockController {
          override def useSnakeCasedParamKeys = true
        }
        ctrl.setAsParams(model)
        implicit val context = ctrl.context

        val params = ctrl.getFromRequestScope[Params](ATTR_PARAMS).get

        params.getAs("text") should be(Some("Text Contents"))
        params.getAs("text_opt") should be(Some("Option Text"))
        params.getAs("number") should be(Some(987L))
        params.getAs("number_opt") should be(Some(-87654321L))
        params.getAs("null_value") should be(None)
        params.getAs("null_value_opt") should be(None)

        params.getAs("date_time") should be(Some(datetime))
        params.getAs("date_time_date") should be(Some("2014-08-23"))
        params.getAs("date_time_time") should be(Some("13:08:27"))
        params.getAs("date_time_year") should be(Some(2014))
        params.getAs("date_time_month") should be(Some(8))
        params.getAs("date_time_day") should be(Some(23))
        params.getAs("date_time_hour") should be(Some(13))
        params.getAs("date_time_minute") should be(Some(8))
        params.getAs("date_time_second") should be(Some(27))

        params.getAs("date_time_opt") should be(Some(datetime2))
        params.getAs("date_time_opt_date") should be(Some("1990-11-03"))
        params.getAs("date_time_opt_time") should be(Some("04:44:00"))
        params.getAs("date_time_opt_year") should be(Some(1990))
        params.getAs("date_time_opt_month") should be(Some(11))
        params.getAs("date_time_opt_day") should be(Some(3))
        params.getAs("date_time_opt_hour") should be(Some(4))
        params.getAs("date_time_opt_minute") should be(Some(44))
        params.getAs("date_time_opt_second") should be(Some(0))

        params.getAs("local_date") should be(Some(localDate))
        params.getAs("local_date_year") should be(Some(2004))
        params.getAs("local_date_month") should be(Some(2))
        params.getAs("local_date_day") should be(Some(29))

        params.getAs("local_date_opt") should be(Some(localDate2))
        params.getAs("local_date_opt_year") should be(Some(1970))
        params.getAs("local_date_opt_month") should be(Some(1))
        params.getAs("local_date_opt_day") should be(Some(1))

        params.getAs("local_time") should be(Some(localTime))
        params.getAs("local_time_hour") should be(Some(14))
        params.getAs("local_time_minute") should be(Some(59))
        params.getAs("local_time_second") should be(Some(1))

        params.getAs("local_time_opt") should be(Some(localTime2))
        params.getAs("local_time_opt_hour") should be(Some(4))
        params.getAs("local_time_opt_minute") should be(Some(0))
        params.getAs("local_time_opt_second") should be(Some(59))
      }
    }

  }
}
