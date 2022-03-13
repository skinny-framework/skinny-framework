package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class requiredSpec extends AnyFlatSpec with Matchers {

  behavior of "required"

  it should "be available" in {
    val validate = required
    validate.name should equal("required")

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
