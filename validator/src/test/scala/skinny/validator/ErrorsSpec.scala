package skinny.validator

import org.scalatest._

class ErrorsSpec extends FlatSpec with Matchers {

  behavior of "Errors"

  it should "be available" in {
    val instance = new Errors(Map())
    instance should not be null
  }

}
