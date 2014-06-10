package skinny.validator

import org.scalatest._

class ErrorSpec extends FlatSpec with Matchers {

  behavior of "Error"

  it should "be available" in {
    val mixedin = new Object with ErrorLike {
      def name = "xxx"
    }
    mixedin should not be null
    mixedin.name should equal("xxx")
  }

}
