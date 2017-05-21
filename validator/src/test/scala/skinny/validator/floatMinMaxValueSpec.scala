package skinny.validator

import org.scalatest._

class floatMinMaxValueSpec extends FlatSpec with Matchers {

  behavior of "floatMinMaxValue"

  it should "be available" in {
    val min      = 2F
    val max      = 5F
    val validate = new floatMinMaxValue(min, max)
    validate.name should equal("floatMinMaxValue")
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
