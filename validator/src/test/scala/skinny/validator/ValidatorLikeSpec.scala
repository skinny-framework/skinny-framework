package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class ValidatorLikeSpec extends FlatSpec with ShouldMatchers {

  behavior of "ValidatorLike"

  it should "be available" in {
    val mixedin = new Object with ValidatorLike {
      val validations: Validations = Validations(Map(), Nil)
    }
    mixedin should not be null
  }

}
