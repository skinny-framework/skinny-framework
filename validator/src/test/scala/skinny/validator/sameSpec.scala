package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class sameSpec extends AnyFlatSpec with Matchers {

  behavior of "same"

  it should "be available" in {
    val validate = same
    validate.name should equal("same")

    validate(param("pair" -> (1, 1))).isSuccess should equal(true)
    validate(param("pair" -> ("a", "a"))).isSuccess should equal(true)
    validate(param("pair" -> (Option("a"), Option("a")))).isSuccess should equal(true)
    validate(param("pair" -> (None, None))).isSuccess should equal(true)

    validate(param("pair" -> ("1", 1))).isSuccess should equal(false)
    validate(param("pair" -> (("a", "b")))).isSuccess should equal(false)
    validate(param("pair" -> (Option("a"), Option("b")))).isSuccess should equal(false)
    validate(param("pair" -> (None, Option("")))).isSuccess should equal(false)
    validate(param("pair" -> (Option("123456"), Option("23456")))).isSuccess should equal(false)

  }

}
