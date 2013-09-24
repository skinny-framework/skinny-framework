package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class notEmptySpec extends FlatSpec with ShouldMatchers {

  behavior of "notEmpty"

  it should "be available" in {
    val validate = notEmpty
    validate.name should equal("notEmpty")

    validate(param("id", null)).isSuccess should equal(false)
    validate(param("id", "")).isSuccess should equal(false)
    validate(param("id", "  ")).isSuccess should equal(false)

    validate(param("id", "   ")).isSuccess should equal(false)

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
  }

}
