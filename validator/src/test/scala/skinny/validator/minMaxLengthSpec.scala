package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class minMaxLengthSpec extends FlatSpec with ShouldMatchers {

  behavior of "minMaxLength"

  it should "be available" in {
    val min: Int = 2
    val max: Int = 3
    val validate = new minMaxLength(min, max)
    validate.name should equal("minMaxLength")
    validate.messageParams should equal(Seq("2", "3"))

    validate(param("id", null)).isSuccess should equal(true)
    validate(param("id", "")).isSuccess should equal(true)

    validate(param("id", "a")).isSuccess should equal(false)
    validate(param("id", "ab")).isSuccess should equal(true)
    validate(param("id", "abc")).isSuccess should equal(true)
    validate(param("id", "abcd")).isSuccess should equal(false)

    validate(param("id", 0)).isSuccess should equal(false)
    validate(param("id", 1)).isSuccess should equal(false)
    validate(param("id", 12)).isSuccess should equal(true)
    validate(param("id", 123)).isSuccess should equal(true)
    validate(param("id", 1234)).isSuccess should equal(false)

    validate(param("id", 0.1)).isSuccess should equal(true)
    validate(param("id", 0.10)).isSuccess should equal(true)
    validate(param("id", 0.01)).isSuccess should equal(false)
    validate(param("id", 0.001)).isSuccess should equal(false)

    validate(param("list", Seq())).isSuccess should equal(false)
    validate(param("list", Seq(1))).isSuccess should equal(false)
    validate(param("list", Seq(1, 2))).isSuccess should equal(true)
    validate(param("list", Seq(1, 2, 3))).isSuccess should equal(true)
    validate(param("list", Seq(1, 2, 3, 4))).isSuccess should equal(false)
  }

}
