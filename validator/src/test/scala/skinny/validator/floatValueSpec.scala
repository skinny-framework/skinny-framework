package skinny.validator

import org.scalatest._

class floatValueSpec extends FlatSpec with Matchers {

  behavior of "floatValue"

  it should "be available" in {
    val validate = floatValue
    validate.name should equal("floatValue")

    validate(param("id", "abc")).isSuccess should equal(false)
    validate(param("id", "あ")).isSuccess should equal(false)
    validate(param("id", "1a")).isSuccess should equal(false)

    validate(param("id", null)).isSuccess should equal(true)
    validate(param("id", "")).isSuccess should equal(true)

    validate(param("id", "0")).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 1L)).isSuccess should equal(true)
    validate(param("id", 1F)).isSuccess should equal(true)
    validate(param("id", 1D)).isSuccess should equal(true)

    validate(param("id", Float.MaxValue)).isSuccess should equal(true)
    validate(param("id", Float.MinValue)).isSuccess should equal(true)
  }
}
