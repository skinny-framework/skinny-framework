package controller

import org.scalatest._
import unit.DBSettings
import skinny.test.MockApiController

class SampleApiControllerSpec extends FunSpec with Matchers with DBSettings {

  def createMockController = new SampleApiController with MockApiController

  describe("SampleApiController") {
    it("creates a company") {
      val controller = createMockController
      controller.prepareParams("name" -> "Typesafe", "url" -> "http://typesafe.com/")
      controller.createCompany
      controller.status should equal(201)
      controller.response.getHeader("Location") should not equal (null)
    }
    it("validates parameters") {
      val controller = createMockController
      controller.prepareParams()
      val response = controller.createCompany
      controller.status should equal(400)
      response should equal(
        """{
          |  "errors" : {
          |    "name" : [ {
          |      "name" : "required",
          |      "message_params" : [ ]
          |    } ]
          |  }
          |}""".stripMargin)
    }
    it("shows companies") {
      val controller = createMockController
      val response = controller.companiesJson
      controller.status should equal(200)
      response should not equal (null)
    }
  }

}
