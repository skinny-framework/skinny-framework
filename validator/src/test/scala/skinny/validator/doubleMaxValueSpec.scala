package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class doubleMaxValueSpec extends AnyFlatSpec with Matchers {

  behavior of "doubleMaxValue"

  it should "be available" in {
    val max      = 3D
    val validate = new doubleMaxValue(max)
    validate.name should equal("doubleMaxValue")

    validate.messageParams should equal(Seq("3.0"))

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)
    validate(param("id", 4)).isSuccess should equal(false)
  }

}
