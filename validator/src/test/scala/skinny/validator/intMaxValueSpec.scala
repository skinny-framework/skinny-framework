package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class intMaxValueSpec extends AnyFlatSpec with Matchers {

  behavior of "intMaxValue"

  it should "be available" in {
    val max      = 3
    val validate = new intMaxValue(max)
    validate.name should equal("intMaxValue")

    validate.messageParams should equal(Seq("3"))

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)
    validate(param("id", 4)).isSuccess should equal(false)
  }

}
