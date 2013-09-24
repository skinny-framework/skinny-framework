package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class ErrorSpec extends FlatSpec with ShouldMatchers {

  behavior of "Error"

  it should "be available" in {
    val mixedin = new Object with Error {
      def name = "xxx"
    }
    mixedin should not be null
    mixedin.name should equal("xxx")
  }

}
