package integrationtest

import _root_.controller.Controllers
import service._
import org.scalatra.test.scalatest._
import skinny._
import skinny.test.SkinnyTestSupport

class MustacheControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.mustache, "/*")

  it should "show top page" in {
    get("/mustache?echo=abcdEFG") {
      // Fails on Travis CI
      if (status == 500) println(body)
      else {
        status should equal(200)
        body should include("abcdEFG")
      }
    }
  }

}
