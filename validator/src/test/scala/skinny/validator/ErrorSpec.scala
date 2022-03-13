package skinny.validator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ErrorSpec extends AnyFlatSpec with Matchers {

  behavior of "Error"

  it should "be available" in {
    val mixedin = new Object with ErrorLike {
      def name = "xxx"
    }
    mixedin should not be null
    mixedin.name should equal("xxx")
  }

}
