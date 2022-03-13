package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class lengthSpec extends AnyFlatSpec with Matchers {

  behavior of "length"

  it should "be available" in {
    val len      = 3
    val validate = new length(len)
    validate.name should equal("length")

    validate(param("x" -> null)).isSuccess should equal(true)
    validate(param("x" -> "")).isSuccess should equal(true)

    validate(param("x" -> "1")).isSuccess should equal(false)
    validate(param("x" -> "12")).isSuccess should equal(false)
    validate(param("x" -> "123")).isSuccess should equal(true)
    validate(param("x" -> 123)).isSuccess should equal(true)
    validate(param("x" -> "1234")).isSuccess should equal(false)
    validate(param("x" -> 1234)).isSuccess should equal(false)
  }

}
