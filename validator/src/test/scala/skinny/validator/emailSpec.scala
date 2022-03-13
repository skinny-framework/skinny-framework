package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class emailSpec extends AnyFlatSpec with Matchers {

  behavior of "email"

  it should "be available" in {
    val validate = email
    validate.name should equal("email")

    validate(param("x" -> null)).isSuccess should equal(true)
    validate(param("x" -> "")).isSuccess should equal(true)

    validate(param("x" -> "  ")).isSuccess should equal(false)
    validate(param("x" -> -123)).isSuccess should equal(false)
    validate(param("x" -> 0)).isSuccess should equal(false)
    validate(param("x" -> 123)).isSuccess should equal(false)
    validate(param("x" -> "xxx")).isSuccess should equal(false)

    validate(param("x" -> "123-abc_DFG@gmail.com")).isSuccess should equal(true)
    validate(param("x" -> "a.b.c@gmail.com")).isSuccess should equal(true)

  }

}
