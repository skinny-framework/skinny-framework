package skinny.filter

import org.scalatra.test.scalatest._
import skinny._
import skinny.controller.AsyncSkinnyController

import scala.util.control.NonFatal

class AsyncErrorPageFilterSpec extends ScalatraFlatSpec {

  behavior of "AsyncErrorPageFilter"

  trait AsyncErrorMessageFilter extends AsyncSkinnyRenderingFilter {

    addRenderingErrorFilter {
      case e: Throwable =>
        logger.error(e.getMessage, e)
        try {
          status = 500
          e.getMessage
        } catch {
          case NonFatal(e) => throw e
        }
    }

  }

  object ErrorController extends AsyncSkinnyController with AsyncErrorMessageFilter with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error")(implicit ctx => execute).as("execute")
  }

  object Error2Controller extends AsyncSkinnyController with AsyncErrorPageFilter with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error2")(implicit ctx => execute).as("execute")
  }
  object Error3Controller
      extends AsyncSkinnyController
      with AsyncErrorPageFilter
      with AsyncErrorMessageFilter
      with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error3")(implicit ctx => execute).as("execute")
  }

  addFilter(ErrorController, "/*")
  addFilter(Error2Controller, "/*")
  addFilter(Error3Controller, "/*")

  it should "render" in {
    get("/error") {
      status should equal(500)
      body should equal("foo-bar-baz")
      header("Content-Type") should startWith("text/html;")
    }

    get("/error2") {
      status should equal(500)
      body should equal(
        """error page
          |<p>Jade footer</p>
          |""".stripMargin
      )
      header("Content-Type") should startWith("text/html;")
    }

    get("/error3") {
      status should equal(500)
      // ErrorPageFilter should be given priority
      body should equal(
        """error page
          |<p>Jade footer</p>
          |""".stripMargin
      )
      header("Content-Type") should startWith("text/html;")
    }
  }

}
