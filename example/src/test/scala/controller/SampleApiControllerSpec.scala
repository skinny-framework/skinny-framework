package controller

import model.{ Company, CompanyId }
import org.scalatest._
import skinny.test.scalatest.ThreadLocalDBAutoRollback
import unit.DBSettings
import skinny.test.MockApiController

class SampleApiControllerSpec extends FunSpec with Matchers with DBSettings with ThreadLocalDBAutoRollback {

  def createMockController = new SampleApiController with MockApiController

  describe("SampleApiController") {
    var createdId: Long = -1

    it("creates a company") {
      val controller = createMockController
      controller.prepareParams("name" -> "Typesafe", "url" -> "http://typesafe.com/")
      controller.createCompany
      controller.status should equal(201)
      controller.response.getHeader("Location") should not equal (null)
      createdId = controller.response.getHeader("Location").split("/").last.toLong
    }

    it("rollbacked after create with ThreadLocalDBAutoRollback") {
      val company = Company.findById(CompanyId(createdId))
      company should equal(None)
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
          |}""".stripMargin
      )
    }

    it("shows companies") {
      val controller = createMockController
      val response   = controller.companiesJson
      controller.status should equal(200)
      response should not equal (null)
    }

    it("output to OutputStream") {
      val controller = createMockController
      controller.responseToOutputStream
      // invalid charset
      controller.getOutputStreamContents should not equal ("ABCDEGあいうえお")
      // valid charset
      controller.getOutputStreamContents("MS932") should equal("ABCDEGあいうえお")
    }
  }

}
