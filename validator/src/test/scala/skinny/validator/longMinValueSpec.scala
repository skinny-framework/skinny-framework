package skinny.validator

import org.scalatest._

class longMinValueSpec extends FlatSpec with Matchers {

  behavior of "longMinValue"

  it should "be available" in {
    val min = 2L
    val validate = new longMinValue(min)
    validate.name should equal("longMinValue")
    validate.messageParams should equal(Seq("2"))

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
