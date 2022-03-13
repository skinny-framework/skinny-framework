package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class notNullSpec extends AnyFlatSpec with Matchers {

  behavior of "notNull"

  it should "be available" in {
    val validate = notNull
    validate.name should equal("notNull")

    validate(param("id", null)).isSuccess should equal(false)

    validate(param("id", "")).isSuccess should equal(true)
    validate(param("id", "  ")).isSuccess should equal(true)

    validate(param("id", "   ")).isSuccess should equal(true)

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
  }

}
