package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class floatMaxValueSpec extends AnyFlatSpec with Matchers {

  behavior of "floatMaxValue"

  it should "be available" in {
    val max      = 3F
    val validate = new floatMaxValue(max)
    validate.name should equal("floatMaxValue")

    validate.messageParams should equal(Seq("3.0"))

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)
    validate(param("id", 4)).isSuccess should equal(false)
  }

}
