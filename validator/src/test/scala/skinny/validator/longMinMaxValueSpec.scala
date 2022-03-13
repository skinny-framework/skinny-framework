package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class longMinMaxValueSpec extends AnyFlatSpec with Matchers {

  behavior of "longMinMaxValue"

  it should "be available" in {
    val min      = 2L
    val max      = 5L
    val validate = new longMinMaxValue(min, max)
    validate.name should equal("longMinMaxValue")
    validate.messageParams should equal(Seq("2", "5"))

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
