package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class maxLengthSpec extends AnyFlatSpec with Matchers {

  behavior of "maxLength"

  it should "be available" in {
    val max: Int = 3
    val validate = new maxLength(max)
    validate.name should equal("maxLength")
    validate.messageParams should equal(Seq("3"))

    validate(param("id", null)).isSuccess should equal(true)
    validate(param("id", "")).isSuccess should equal(true)

    validate(param("id", "a")).isSuccess should equal(true)
    validate(param("id", "ab")).isSuccess should equal(true)
    validate(param("id", "abc")).isSuccess should equal(true)
    validate(param("id", "abcd")).isSuccess should equal(false)

    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 12)).isSuccess should equal(true)
    validate(param("id", 123)).isSuccess should equal(true)
    validate(param("id", 1234)).isSuccess should equal(false)

    validate(param("id", 0.1)).isSuccess should equal(true)
    validate(param("id", 0.10)).isSuccess should equal(true)
    validate(param("id", 0.01)).isSuccess should equal(false)
    validate(param("id", 0.001)).isSuccess should equal(false)

    validate(param("list", Seq())).isSuccess should equal(true)
    validate(param("list", Seq(1))).isSuccess should equal(true)
    validate(param("list", Seq(1, 2))).isSuccess should equal(true)
    validate(param("list", Seq(1, 2, 3))).isSuccess should equal(true)

    validate(param("list", Seq(1, 2, 3, 4))).isSuccess should equal(false)
  }

}
