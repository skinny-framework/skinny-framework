package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ErrorsSpec extends AnyFlatSpec with Matchers {

  behavior of "Errors"

  it should "be available" in {
    val instance = new Errors(Map())
    instance should not be null
  }

}
