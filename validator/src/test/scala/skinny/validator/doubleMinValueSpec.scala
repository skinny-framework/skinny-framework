package skinny.validator

import org.scalatest._

class doubleMinValueSpec extends FlatSpec with Matchers {

  behavior of "doubleMinValue"

  it should "be available" in {
    val min      = 2D
    val validate = new doubleMinValue(min)
    validate.name should equal("doubleMinValue")
    validate.messageParams should equal(Seq("2.0"))

    validate(KeyValueParamDefinition("id", -1)).isSuccess should equal(false)
    validate(KeyValueParamDefinition("id", 0)).isSuccess should equal(false)
    validate(KeyValueParamDefinition("id", 1)).isSuccess should equal(false)
    validate(KeyValueParamDefinition("id", 2)).isSuccess should equal(true)
    validate(KeyValueParamDefinition("id", 3)).isSuccess should equal(true)
    validate(KeyValueParamDefinition("id", 4)).isSuccess should equal(true)
    validate(KeyValueParamDefinition("id", 5)).isSuccess should equal(true)
    validate(KeyValueParamDefinition("id", 6)).isSuccess should equal(true)
    validate(KeyValueParamDefinition("id", 7)).isSuccess should equal(true)
  }

}
