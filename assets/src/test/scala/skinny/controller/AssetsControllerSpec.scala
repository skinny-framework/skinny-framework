package skinny.controller

import javax.servlet.http.HttpServletRequest

import org.scalatest._
import org.scalatest.mock.MockitoSugar

class AssetsControllerSpec extends FlatSpec with Matchers with MockitoSugar {

  val controller = new AssetsController {
    override def request = mock[HttpServletRequest]
  }

  it should "be available" in {
    // TODO: better tests
    intercept[Throwable] {
      controller.css()
    }
    intercept[Throwable] {
      controller.js()
    }
  }

}
