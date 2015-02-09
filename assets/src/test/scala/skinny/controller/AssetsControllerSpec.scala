package skinny.controller

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import javax.servlet.{ Filter, ServletContext }

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.anyString
import org.mockito.internal.stubbing.answers._
import skinny.SkinnyEnv

class AssetsControllerSpec extends FlatSpec with Matchers with MockitoSugar {
  class MockPassException extends Throwable

  def newController = new AssetsController() {
    override def request = mock[HttpServletRequest]
    override def response = mock[HttpServletResponse]

    private val _servletContext = {
      val sc = mock[ServletContext]
      when(sc.getRealPath(anyString())).thenAnswer(new ReturnsArgumentAt(0))
      sc
    }
    override def servletContext: ServletContext = _servletContext

    var _path: String = ""
    override def multiParams(key: String)(implicit request: HttpServletRequest): Seq[String] = {
      if (key == "splat") Seq(_path) else Seq.empty
    }

    var _isDisabledInStaging: Boolean = true
    override def isDisabledInStaging: Boolean = _isDisabledInStaging
    var _isDisabledInProduction: Boolean = true
    override def isDisabledInProduction: Boolean = _isDisabledInProduction

    override def pass() = throw new MockPassException
  }

  it should "be available" in {
    AssetsController.jsRootUrl should not equal (null)
    AssetsController.cssRootUrl should not equal (null)

    val controller = newController

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

  it should "pass with non-supported file" in {
    val controller = newController
    controller._path = "file.txt"

    intercept[MockPassException] { controller.css() }
    intercept[MockPassException] { controller.js() }
  }

  it should "pass when file is not exists" in {
    val controller = newController
    intercept[MockPassException] {
      controller._path = "notexists.js"
      controller.js()
    }
    intercept[MockPassException] {
      controller._path = "notexists.css"
      controller.css()
    }
  }

  it should "work in each envs" in {
    val controller = newController

    for (
      env <- Seq("", "staging", "production");
      disInStaging <- Seq(true, false);
      disInProduction <- Seq(true, false)
    ) withClue(s"env: ${env}, " +
      s"disabled: { staging: ${disInStaging}," +
      s" prod: ${disInProduction} }") {

      System.setProperty(SkinnyEnv.PropertyKey, env)
      controller._isDisabledInStaging = disInStaging
      controller._isDisabledInProduction = disInProduction
      if ((env == "staging" && disInStaging) ||
        (env == "production" && disInProduction)) {
        intercept[MockPassException] {
          controller._path = "AssetsControllerSpec.js"
          controller.js()
        }
        intercept[MockPassException] {
          controller._path = "AssetsControllerSpec.css"
          controller.css()
        }
      } else {
        controller._path = "AssetsControllerSpec.js"
        controller.js().toString should include("AssetsControllerSpec#js")
        controller._path = "AssetsControllerSpec.css"
        controller.css().toString should include("AssetsControllerSpec#css")
      }
    }
  }
}
