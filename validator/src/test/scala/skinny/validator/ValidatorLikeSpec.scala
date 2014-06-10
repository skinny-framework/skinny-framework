package skinny.validator

import org.scalatest._

class ValidatorLikeSpec extends FlatSpec with Matchers {

  behavior of "ValidatorLike"

  it should "be available" in {
    val mixedin = new Object with ValidatorLike {
      val validations: Validations = Validations(Map(), Nil)
    }
    mixedin should not be null
  }

}
