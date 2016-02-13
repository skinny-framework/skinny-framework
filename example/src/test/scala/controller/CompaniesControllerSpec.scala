package controller

import org.scalatest._
import skinny.test.{ MockController, FactoryGirl }
import model.Company
import skinny.Format
import unit.DBSettings

class CompaniesControllerSpec extends FunSpec with Matchers with DBSettings {

  describe("CompaniesController") {

    def newCompany = FactoryGirl(Company).create()
    def createMockController = new CompaniesController with MockController

    describe("shows resources") {
      it("shows HTML response") {
        val controller = createMockController
        controller.showResources()

        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/companies/index"))
        controller.contentType should equal("text/html; charset=utf-8")
      }

      it("shows JSON response") {
        val controller = createMockController
        implicit val format = Format.JSON
        controller.showResources()

        controller.status should equal(200)
        controller.contentType should equal("application/json; charset=utf-8")
      }
    }

    describe("shows a resource") {
      it("shows HTML response") {
        val company = newCompany
        val controller = createMockController
        controller.showResource(company.id)

        controller.status should equal(200)
        controller.getFromRequestScope[Company]("item") should equal(Some(company))
        controller.renderCall.map(_.path) should equal(Some("/companies/show"))
      }
    }

    describe("shows new resource form") {
      it("shows HTML response") {
        val controller = createMockController
        controller.newResource()
        controller.status should equal(200)
      }
    }

    describe("creates a resource") {
      it("succeeds with valid parameters") {
        val controller = createMockController
        val newName = s"Created at ${System.currentTimeMillis}"
        controller.prepareParams(
          "name" -> newName,
          "url" -> "http://www.example.com/",
          "updatedAt" -> "2013-01-02 12:34:56"
        )
        controller.createResource()
        controller.status should equal(200)
      }

      it("fails with invalid parameters") {
        val controller = createMockController
        controller.prepareParams("url" -> "http://www.example.com/")
        controller.createResource()
        controller.status should equal(400)
        controller.errorMessages.size should equal(2)
      }
    }

    it("shows edit form") {
      val company = newCompany
      val controller = createMockController
      controller.editResource(company.id)
      controller.status should equal(200)
    }

    it("updates a resource") {
      val company = newCompany
      val controller = createMockController
      controller.prepareParams(
        "id" -> company.id.value.toString,
        "name" -> s"Updated at ${System.currentTimeMillis}",
        "updatedAt" -> "2013-01-02 12:34:56"
      )
      controller.updateResource(company.id)
      controller.status should equal(200)
    }

    it("destroys a resource") {
      val company = newCompany
      val controller = createMockController
      controller.destroyResource(company.id)
      controller.status should equal(200)
    }

  }
}
