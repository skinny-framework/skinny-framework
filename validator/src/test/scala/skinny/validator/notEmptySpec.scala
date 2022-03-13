package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class notEmptySpec extends AnyFlatSpec with Matchers {

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
