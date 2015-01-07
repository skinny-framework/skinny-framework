package skinny.controller

import javax.servlet.http.HttpServletRequest

import org.scalatest._
import org.scalatest.mock.MockitoSugar

class AssetsControllerSpec extends FlatSpec with Matchers with MockitoSugar {

  val controller = new AssetsController {
    override def request = mock[HttpServletRequest]
  }

  it should "be available" in {
    AssetsController.jsRootUrl should not equal (null)
    AssetsController.cssRootUrl should not equal (null)

    // TODO: better tests
    intercept[Throwable] {
      controller.css()
    }
    intercept[Throwable] {
      controller.js()
    }
    controller.assetsRootPath should equal("/assets")
    controller.jsRootPath should equal("/assets/js")
    controller.cssRootPath should equal("/assets/css")
    controller.isDisabledInStaging should equal(true)
    controller.isDisabledInProduction should equal(true)
    controller.isEnabled should equal(true)
  }

}
