package integrationtest

import controller.Controllers
import org.scalatra.test.scalatest._

class ScaldiControllerSpec extends ScalatraFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.scaldi, "/*")

  it should "show page" in {
    get("/scaldi/", "value" -> "foo") {
      logBodyUnless(200)
      status should equal(200)
      body should equal("FOO")
    }
  }

}
