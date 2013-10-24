package controller

import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport

class ThymeleafControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(Controllers.thymeleaf, "/*")

  it should "render as expected" in {
    get("/thymeleaf/") {
      status should equal(200)
      body should include("Hello, Thymeleaf!")
    }
  }

}
