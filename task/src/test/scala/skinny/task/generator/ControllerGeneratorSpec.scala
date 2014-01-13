package skinny.task.generator

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class ControllerGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ControllerGenerator

  describe("Controller") {
    it("should be created as expected") {
      val code = generator.code("members")
      val expected =
        """package controller
          |
          |import skinny._
          |import skinny.validator._
          |
          |class MembersController extends ApplicationController {
          |  protectFromForgery()
          |
          |  def index = render("/members/index")
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("ControllerSpec") {
    it("should be created as expected") {
      val code = generator.spec("members")
      val expected =
        """package controller
          |
          |import _root_.controller._
          |import _root_.model._
          |import org.scalatra.test.scalatest._
          |import skinny.test._
          |
          |class MembersControllerSpec extends ScalatraFlatSpec {
          |  addFilter(Controllers.members, "/*")
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
