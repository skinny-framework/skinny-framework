package skinny.validator

import org.scalatest._

class checkAllSpec extends FlatSpec with Matchers {

  behavior of "checkAll"

  it should "work with Int builtins" in {
    val validations = checkAll(numeric, intValue, intMinMaxValue(-200, 100))
    validations.name should equal("combined-results")

    validations(param("x" -> null)).isSuccess should equal(true)
    validations(param("x" -> "")).isSuccess should equal(true)
    validations(param("x" -> "  ")).isSuccess should equal(false)

    validations(param("x" -> -123)).isSuccess should equal(true)
    validations(param("x" -> 0)).isSuccess should equal(true)
    validations(param("x" -> "90")).isSuccess should equal(true)

    validations(param("x" -> 123)).isSuccess should equal(false)
    validations(param("x" -> 15.2)).isSuccess should equal(false)

    validations(param("x" -> "xxx")).isSuccess should equal(false)
  }

  it should "work with Long builtins" in {
    val validations = checkAll(numeric, longValue, longMinMaxValue(-200, 100))
    validations.name should equal("combined-results")

    validations(param("x" -> null)).isSuccess should equal(true)
    validations(param("x" -> "")).isSuccess should equal(true)
    validations(param("x" -> "  ")).isSuccess should equal(false)

    validations(param("x" -> -123)).isSuccess should equal(true)
    validations(param("x" -> 0)).isSuccess should equal(true)
    validations(param("x" -> "90")).isSuccess should equal(true)

    validations(param("x" -> 123)).isSuccess should equal(false)
    validations(param("x" -> 15.2)).isSuccess should equal(false)

    validations(param("x" -> "xxx")).isSuccess should equal(false)
  }

  it should "work with Double builtins" in {
    val validations = checkAll(numeric, doubleValue, doubleMinMaxValue(-200, 100))
    validations.name should equal("combined-results")

    validations(param("x" -> null)).isSuccess should equal(true)
    validations(param("x" -> "")).isSuccess should equal(true)
    validations(param("x" -> "  ")).isSuccess should equal(false)

    validations(param("x" -> -123)).isSuccess should equal(true)
    validations(param("x" -> -123.51)).isSuccess should equal(true)
    validations(param("x" -> 0)).isSuccess should equal(true)
    validations(param("x" -> "90")).isSuccess should equal(true)
    validations(param("x" -> "90.3")).isSuccess should equal(true)
    validations(param("x" -> 15.2)).isSuccess should equal(true)

    validations(param("x" -> 123)).isSuccess should equal(false)

    validations(param("x" -> "xxx")).isSuccess should equal(false)
  }

  it should "work with Float builtins" in {
    val validations = checkAll(numeric, floatValue, floatMinMaxValue(-200, 100))
    validations.name should equal("combined-results")

    validations(param("x" -> null)).isSuccess should equal(true)
    validations(param("x" -> "")).isSuccess should equal(true)
    validations(param("x" -> "  ")).isSuccess should equal(false)

    validations(param("x" -> -123)).isSuccess should equal(true)
    validations(param("x" -> -123.51)).isSuccess should equal(true)
    validations(param("x" -> 0)).isSuccess should equal(true)
    validations(param("x" -> "90")).isSuccess should equal(true)
    validations(param("x" -> "90.3")).isSuccess should equal(true)
    validations(param("x" -> 15.2)).isSuccess should equal(true)

    validations(param("x" -> 123)).isSuccess should equal(false)

    validations(param("x" -> "xxx")).isSuccess should equal(false)
  }

}
