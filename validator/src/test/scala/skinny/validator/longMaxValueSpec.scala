package skinny.validator

import org.scalatest._

class longMaxValueSpec extends FlatSpec with Matchers {

  behavior of "longMaxValue"

  it should "be available" in {
    val max = 3L
    val validate = new longMaxValue(max)
    validate.name should equal("longMaxValue")
    validate.messageParams should equal(Seq("3"))

    validate(param("id", -1)).isSuccess should equal(true)
    validate(param("id", 0)).isSuccess should equal(true)
    validate(param("id", 1)).isSuccess should equal(true)
    validate(param("id", 2)).isSuccess should equal(true)
    validate(param("id", 3)).isSuccess should equal(true)
    validate(param("id", 4)).isSuccess should equal(false)
  }

}
