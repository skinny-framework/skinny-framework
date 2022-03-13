package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class doubleMinMaxValueSpec extends AnyFlatSpec with Matchers {

  behavior of "doubleMinMaxValue"

  it should "be available" in {
    val min      = 2D
    val max      = 5D
    val validate = new doubleMinMaxValue(min, max)
    validate.name should equal("doubleMinMaxValue")
    validate.messageParams should equal(Seq("2.0", "5.0"))

    validate(param("id", -1)).isSuccess should equal(false)
    validate(param("id", 0)).isSuccess should equal(false)
    validate(param("id", 1)).isSuccess should equal(false)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)
    validate(param("id", 4)).isSuccess should equal(true)
    validate(param("id", 5)).isSuccess should equal(true)
    validate(param("id", 6)).isSuccess should equal(false)
    validate(param("id", 7)).isSuccess should equal(false)
  }

}
