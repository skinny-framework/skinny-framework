package integrationtest

import skinny.test.SkinnyTestSupport
import controller.ErrorController
import model.Company
import skinny.test.scalatest.SkinnyFlatSpec
import unit.DBSettings

class ErrorControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport with DBSettings {

  addFilter(ErrorController, "/*")

  it should "return error as expected" in {
    get("/error/runtime") {
      status should equal(500)
    }
  }

  it should "roll back without exception" in {
    val before = Company.count()
    get("/error/rollback") {
      status should equal(200)
      val after = Company.count()
      after should equal(before)
    }
  }

}
