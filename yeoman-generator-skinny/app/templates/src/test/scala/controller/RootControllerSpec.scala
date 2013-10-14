package controller

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport

class RootControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.root, "/*")

  it should "show top page" in {
    get("/") {
      status should equal(200)
    }
  }

}
