package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ValidationsSpec extends AnyFlatSpec with Matchers {

  behavior of "Validations"

  it should "be available" in {
    val results  = Nil
    val instance = Validations(Map(), results)
    instance should not be null
  }

}
