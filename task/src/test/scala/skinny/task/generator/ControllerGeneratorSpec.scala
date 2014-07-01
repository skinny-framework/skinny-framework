package skinny.task.generator

import org.scalatest._

class ControllerGeneratorSpec extends FunSpec with Matchers {

  val generator = ControllerGenerator

  describe("Controller") {
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
      val code = generator.spec(Seq("admin"), "members")
      val expected =
        """package controller.admin
          |
          |import _root_.controller._
          |import _root_.model._
          |import org.scalatra.test.scalatest._
          |import org.scalatest._
          |import skinny.test._
          |
          |class MembersControllerSpec extends ScalatraFlatSpec with Matchers {
          |  addFilter(Controllers.adminMembers, "/*")
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
