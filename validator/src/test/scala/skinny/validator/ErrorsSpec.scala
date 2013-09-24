package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class ErrorsSpec extends FlatSpec with ShouldMatchers {

  behavior of "Errors"

  it should "be available" in {
    val instance = new Errors(Map())
    instance should not be null
  }

}
