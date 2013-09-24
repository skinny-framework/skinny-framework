package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class intMinMaxValueSpec extends FlatSpec with ShouldMatchers {

  behavior of "intMinMaxValue"

  it should "be available" in {
    val min: Int = 2
    val max: Int = 5
    val validate = new intMinMaxValue(min, max)
    validate.name should equal("intMinMaxValue")
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
