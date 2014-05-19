package skinny.validator

import org.scalatest._

class floatMinValueSpec extends FlatSpec with Matchers {

  behavior of "floatMinValue"

  it should "be available" in {
    val min = 2F
    val validate = new floatMinValue(min)
    validate.name should equal("floatMinValue")
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
