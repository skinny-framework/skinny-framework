package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ValidatorLikeSpec extends AnyFlatSpec with Matchers {

  behavior of "ValidatorLike"

  it should "be available" in {
    val mixedin = new Object with ValidatorLike {
      val validations: Validations = Validations(Map(), Nil)
    }
    mixedin should not be null
  }

}
