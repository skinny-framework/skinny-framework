package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class intValueSpec extends AnyFlatSpec with Matchers {

  behavior of "intValue"

  it should "be available" in {
    val validate = intValue
    validate.name should equal("intValue")

    validate(param("id", "abc")).isSuccess should equal(false)
    validate(param("id", "あ")).isSuccess should equal(false)
    validate(param("id", "1a")).isSuccess should equal(false)

    validate(param("id", null)).isSuccess should equal(true)
    validate(param("id", "")).isSuccess should equal(true)

    validate(param("id", "0")).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", -0.1D)).isSuccess should equal(false)

    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)

    validate(param("id", java.lang.Integer.MAX_VALUE)).isSuccess should equal(true)
    validate(param("id", s"${java.lang.Integer.MAX_VALUE}1")).isSuccess should equal(false)
    validate(param("id", java.lang.Integer.MIN_VALUE)).isSuccess should equal(true)
    validate(param("id", s"${java.lang.Integer.MIN_VALUE}1")).isSuccess should equal(false)

  }

}
