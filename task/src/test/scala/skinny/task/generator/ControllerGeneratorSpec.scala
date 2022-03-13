package skinny.task.generator

import org.scalatest._

class ControllerGeneratorSpec extends FunSpec with Matchers {

  val generator = ControllerGenerator

  describe("Controller") {

    it("should show usage") {
      generator.run(List())
    }

    it("should be created as expected") {
      val code = generator.code(Seq("admin"), "members")
      val expected =
        """package controller.admin
          |
          |import _root_.controller._
          |import skinny._
          |import skinny.validator._
          |
          |class MembersController extends ApplicationController {
          |  protectFromForgery()
          |
          |  def index = render("/admin/members/index")
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("ControllerSpec") {
    it("should be created as expected") {
      val code = generator.controllerSpec(Seq("admin"), "members")
      val expected =
        """package controller.admin
          |
          |import org.scalatest.funspec.AnyFunSpec
          |import org.scalatest.matchers.should.Matchers
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |
          |// NOTICE before/after filters won't be executed by default
          |class MembersControllerSpec extends AnyFunSpec with Matchers with DBSettings {
          |
          |  def createMockController = new MembersController with MockController
          |
          |  describe("MembersController") {
          |
          |    it("shows index page") {
          |      val controller = createMockController
          |      controller.index
          |      controller.status should equal(200)
          |      controller.renderCall.map(_.path) should equal(Some("/admin/members/index"))
          |      controller.contentType should equal("text/html; charset=utf-8")
          |    }
          |
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("IntegrationTestSpec") {
    it("should be created as expected") {
      val code = generator.integrationSpec(Seq("admin"), "members")
      val expected =
        """package integrationtest.admin
          |
          |import org.scalatest._
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |import _root_.controller.Controllers
          |
          |class MembersController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport {
          |  addFilter(Controllers.adminMembers, "/*")
          |
          |  it should "show index page" in {
          |    get("/admin/members") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
