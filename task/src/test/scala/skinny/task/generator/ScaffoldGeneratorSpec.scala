package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldSspGenerator

  describe("Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerCode("members", "member", "ssp", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """package controller
          |
          |import skinny._
          |import skinny.validator._
          |import model.Member
          |
          |object MembersController extends SkinnyResource {
          |  protectFromForgery()
          |
          |  override def model = Member
          |  override def resourcesName = "members"
          |  override def resourceName = "member"
          |
          |  override def createForm = validation(
          |    paramKey("name") is required,
          |    paramKey("favoriteNumber") is required & numeric & longValue,
          |    paramKey("magicNumber") is numeric & intValue
          |  )
          |  override def createFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favoriteNumber" -> ParamType.Long,
          |    "magicNumber" -> ParamType.Int,
          |    "isActivated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate
          |  )
          |
          |  override def updateForm = validation(
          |    paramKey("name") is required,
          |    paramKey("favoriteNumber") is required & numeric & longValue,
          |    paramKey("magicNumber") is numeric & intValue
          |  )
          |  override def updateFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favoriteNumber" -> ParamType.Long,
          |    "magicNumber" -> ParamType.Int,
          |    "isActivated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate
          |  )
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("Spec for Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerSpecCode("members", "member", Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """package controller
          |
          |import org.scalatra.test.scalatest._
          |import skinny._, test._
          |import org.joda.time._
          |import model._
          |
          |class MembersControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {
          |  addFilter(MembersController, "/*")
          |
          |  def member = FactoryGirl(Member).create()
          |
          |  it should "show members" in {
          |    get("/members") {
          |      status should equal(200)
          |    }
          |    get("/members/") {
          |      status should equal(200)
          |    }
          |    get("/members.json") {
          |      status should equal(200)
          |    }
          |    get("/members.xml") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a member in detail" in {
          |    get(s"/members/${member.id}") {
          |      status should equal(200)
          |    }
          |    get(s"/members/${member.id}.xml") {
          |      status should equal(200)
          |    }
          |    get(s"/members/${member.id}.json") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/members/new") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a member" in {
          |    post(s"/members", "name" -> "dummy","isActivated" -> "true","birthday" -> new LocalDate().toString()) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrfToken" -> "12345") {
          |      post(s"/members", "name" -> "dummy","isActivated" -> "true","birthday" -> new LocalDate().toString(), "csrfToken" -> "12345") {
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        Member.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/members/${member.id}/edit") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a member" in {
          |    put(s"/members/${member.id}", "name" -> "dummy","isActivated" -> "true","birthday" -> new LocalDate().toString()) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrfToken" -> "12345") {
          |      put(s"/members/${member.id}", "name" -> "dummy","isActivated" -> "true","birthday" -> new LocalDate().toString(), "csrfToken" -> "12345") {
          |        status should equal(200)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    val member = FactoryGirl(Member).create()
          |    delete(s"/members/${member.id}") {
          |      status should equal(403)
          |    }
          |    withSession("csrfToken" -> "aaaaaa") {
          |      delete(s"/members/${member.id}?csrfToken=aaaaaa") {
          |        status should equal(200)
          |      }
          |    }
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("messages.conf") {
    it("should be created as expected") {
      val code = generator.messagesConfCode("members", "member", Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """
          |member {
          |  flash {
          |    created="The member was created."
          |    updated="The member was updated."
          |    deleted="The member was deleted."
          |  }
          |  list="Members"
          |  detail="Member"
          |  edit="Edit Member"
          |  new="New Member"
          |  delete.confirm="Are you sure?"
          |  id="ID"
          |  name="Name"
          |  isActivated="IsActivated"
          |  birthday="Birthday"
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("db/migration/xxx.sql") {
    it("should be created as expected") {
      val code = generator.migrationSQL("members", "member", Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-- For H2 Database
          |create table members (
          |  id bigserial not null primary key,
          |  name varchar(512) not null,
          |  is_activated boolean not null,
          |  birthday date,
          |  created_at timestamp not null,
          |  updated_at timestamp
          |)
          |""".stripMargin
      code should equal(expected)
    }
  }

}
