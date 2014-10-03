package skinny.validator

import org.scalatest._

class intMinValueSpec extends FlatSpec with Matchers {

  behavior of "intMinValue"

  it should "be available" in {
    val min: Int = 2
    val validate = new intMinValue(min)
    validate.name should equal("intMinValue")

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
