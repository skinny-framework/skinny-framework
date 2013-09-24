package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class longMinMaxValueSpec extends FlatSpec with ShouldMatchers {

  behavior of "longMinMaxValue"

  it should "be available" in {
    val min = 2L
    val max = 5L
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
