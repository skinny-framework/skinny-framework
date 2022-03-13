package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.joda.time._

class pastSpec extends AnyFlatSpec with Matchers {

  behavior of "past"

  it should "be available" in {
    val validate = past
    validate.name should equal("past")

    validate(param("x" -> null)).isSuccess should equal(false)

    validate(param("x" -> DateTime.now.minusDays(1))).isSuccess should equal(true)

    val judPast = DateTime.now.minusDays(1).toDate
    validate(param("x" -> judPast)).isSuccess should equal(true)

    validate(param("x" -> DateTime.now.plusDays(1))).isSuccess should equal(false)

    val judFuture = DateTime.now.plusDays(1).toDate
    validate(param("x" -> judFuture)).isSuccess should equal(false)
  }

}
