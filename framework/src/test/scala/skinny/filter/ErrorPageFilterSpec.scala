package skinny.filter

import skinny._
import org.scalatra.test.scalatest._
import scala.util.control.NonFatal

class ErrorPageFilterSpec extends ScalatraFlatSpec {

  behavior of "ErrorPageFilter"

  trait ErrorMessageFilter extends SkinnyRenderingFilter {

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

  object ErrorController extends SkinnyController with ErrorMessageFilter with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error")(execute).as("execute")
  }

  object Error2Controller extends SkinnyController with ErrorPageFilter with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error2")(execute).as("execute")
  }
  object Error3Controller extends SkinnyController with ErrorPageFilter with ErrorMessageFilter with Routes {
    def execute = throw new RuntimeException("foo-bar-baz")
    get("/error3")(execute).as("execute")
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
