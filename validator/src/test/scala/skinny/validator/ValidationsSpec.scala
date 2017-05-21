package skinny.validator

import org.scalatest._

class ValidationsSpec extends FlatSpec with Matchers {

  behavior of "Validations"

  it should "be available" in {
    val results  = Nil
    val instance = Validations(Map(), results)
    instance should not be null
  }

}
