package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class doubleValueSpec extends FlatSpec with ShouldMatchers {

  behavior of "doubleValue"

  it should "be available" in {
    val validate = doubleValue
    validate.name should equal("doubleValue")

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

    validate(param("id", Double.MaxValue)).isSuccess should equal(true)
    validate(param("id", Double.MinValue)).isSuccess should equal(true)
  }
}
