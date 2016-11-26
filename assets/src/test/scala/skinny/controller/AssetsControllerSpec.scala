package skinny.controller

import scala.language.reflectiveCalls

import javax.servlet.ServletContext

import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.anyString
import org.mockito.internal.stubbing.answers._
import skinny.SkinnyEnv
import skinny.micro.context.SkinnyContext
import skinny.test.MockController

class AssetsControllerSpec extends FlatSpec with Matchers with MockitoSugar {
  class MockPassException extends Throwable

  def newController = new AssetsController with MockController {

    override val servletContext: ServletContext = {
      val sc = mock[ServletContext]
      when(sc.getRealPath(anyString())).thenAnswer(new ReturnsArgumentAt(0))
      sc
    }

    var _path: String = ""
    override def multiParams(key: String)(implicit ctx: SkinnyContext): Seq[String] = {
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
      disabledInStaging <- Seq(true, false);
      disabledInProduction <- Seq(true, false)
    ) withClue(s"env: ${env}, " +
      s"disabled: { staging: ${disabledInStaging}," +
      s" prod: ${disabledInProduction} }") {

      System.setProperty(SkinnyEnv.PropertyKey, env)
      controller._isDisabledInStaging = disabledInStaging
      controller._isDisabledInProduction = disabledInProduction
      if ((env == "staging" && disabledInStaging) ||
        (env == "production" && disabledInProduction)) {
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

  it should "work sub directories" in {
    val controller = newController

    for (
      env <- Seq("", "staging", "production");
      disabledInStaging <- Seq(true, false);
      disabledInProduction <- Seq(true, false)
    ) withClue(s"env: ${env}, " +
      s"disabled: { staging: ${disabledInStaging}," +
      s" prod: ${disabledInProduction} }") {

      System.setProperty(SkinnyEnv.PropertyKey, env)
      controller._isDisabledInStaging = disabledInStaging
      controller._isDisabledInProduction = disabledInProduction
      if ((env == "staging" && disabledInStaging) ||
        (env == "production" && disabledInProduction)) {
        intercept[MockPassException] {
          controller._path = "vendor/awesome.js"
          controller.js()
        }
        intercept[MockPassException] {
          controller._path = "vendor/awesome.css"
          controller.css()
        }
      } else {
        controller._path = "vendor/awesome.js"
        controller.js().toString should include("Awesome! :+1:")
        controller._path = "vendor/awesome.css"
        controller.css().toString should include(".awesome {")

        controller._path = "vendor/awesome.min.js"
        controller.js().toString should include("Awesome! :+1:")
        controller._path = "vendor/you.are.great.css"
        controller.css().toString should include(".you-are-great {")

        controller._path = "vendor/jquery-ui-1.11.1.custom/jquery-ui.min.css"
        controller.css().toString should include(""".ui {""")
      }
    }
  }
}
