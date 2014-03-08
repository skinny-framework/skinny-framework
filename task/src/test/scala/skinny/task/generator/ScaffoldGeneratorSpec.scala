package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldSspGenerator

  describe("Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerCode(Seq("admin"), "members", "member", "ssp", Seq(
        ScaffoldGeneratorArg("name", "String", None),
        ScaffoldGeneratorArg("favoriteIntNumber", "Int", None),
        ScaffoldGeneratorArg("favoriteLongNumber", "Long", None),
        ScaffoldGeneratorArg("favoriteShortNumber", "Short", None),
        ScaffoldGeneratorArg("favoriteDoubleNumber", "Double", None),
        ScaffoldGeneratorArg("favoriteFloatNumber", "Float", None),
        ScaffoldGeneratorArg("magicNumber", "Option[Int]", None),
        ScaffoldGeneratorArg("isActivated", "Boolean", None),
        ScaffoldGeneratorArg("birthday", "Option[LocalDate]", None),
        ScaffoldGeneratorArg("timeToWakeUp", "Option[LocalTime]", None),
        ScaffoldGeneratorArg("createdAt", "DateTime", None)
      ))

      val expected =
        """package controller.admin
          |
          |import skinny._
          |import skinny.validator._
          |import _root_.controller._
          |import model.admin.Member
          |
          |object MembersController extends SkinnyResource with ApplicationController {
          |  protectFromForgery()
          |
          |  override def model = Member
          |  override def resourcesName = "members"
          |  override def resourceName = "member"
          |
          |  override def resourcesBasePath = s"/admin/${toSnakeCase(resourcesName)}"
          |  override def viewsDirectoryPath = s"/admin/${toSnakeCase(resourcesName)}"
          |  override def useSnakeCasedParamKeys = true
          |
          |  override def createParams = Params(params).withDate("birthday").withTime("time_to_wake_up").withDateTime("created_at")
          |  override def createForm = validation(createParams,
          |    paramKey("name") is required & maxLength(512),
          |    paramKey("favorite_int_number") is required & numeric & intValue,
          |    paramKey("favorite_long_number") is required & numeric & longValue,
          |    paramKey("favorite_short_number") is required & numeric & intValue,
          |    paramKey("favorite_double_number") is required & doubleValue,
          |    paramKey("favorite_float_number") is required & floatValue,
          |    paramKey("magic_number") is numeric & intValue,
          |    paramKey("birthday") is dateFormat,
          |    paramKey("time_to_wake_up") is timeFormat,
          |    paramKey("created_at") is required & dateTimeFormat
          |  )
          |  override def createFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favorite_int_number" -> ParamType.Int,
          |    "favorite_long_number" -> ParamType.Long,
          |    "favorite_short_number" -> ParamType.Short,
          |    "favorite_double_number" -> ParamType.Double,
          |    "favorite_float_number" -> ParamType.Float,
          |    "magic_number" -> ParamType.Int,
          |    "is_activated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate,
          |    "time_to_wake_up" -> ParamType.LocalTime,
          |    "created_at" -> ParamType.DateTime
          |  )
          |
          |  override def updateParams = Params(params).withDate("birthday").withTime("time_to_wake_up").withDateTime("created_at")
          |  override def updateForm = validation(updateParams,
          |    paramKey("name") is required & maxLength(512),
          |    paramKey("favorite_int_number") is required & numeric & intValue,
          |    paramKey("favorite_long_number") is required & numeric & longValue,
          |    paramKey("favorite_short_number") is required & numeric & intValue,
          |    paramKey("favorite_double_number") is required & doubleValue,
          |    paramKey("favorite_float_number") is required & floatValue,
          |    paramKey("magic_number") is numeric & intValue,
          |    paramKey("birthday") is dateFormat,
          |    paramKey("time_to_wake_up") is timeFormat,
          |    paramKey("created_at") is required & dateTimeFormat
          |  )
          |  override def updateFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favorite_int_number" -> ParamType.Int,
          |    "favorite_long_number" -> ParamType.Long,
          |    "favorite_short_number" -> ParamType.Short,
          |    "favorite_double_number" -> ParamType.Double,
          |    "favorite_float_number" -> ParamType.Float,
          |    "magic_number" -> ParamType.Int,
          |    "is_activated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate,
          |    "time_to_wake_up" -> ParamType.LocalTime,
          |    "created_at" -> ParamType.DateTime
          |  )
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("Spec for Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerSpecCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteIntNumber" -> "Int",
        "favoriteLongNumber" -> "Long",
        "favoriteShortNumber" -> "Short",
        "favoriteDoubleNumber" -> "Double",
        "favoriteFloatNumber" -> "Float",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """package controller.admin
          |
          |import org.scalatra.test.scalatest._
          |import skinny._, test._
          |import org.joda.time._
          |import model.admin._
          |
          |class MembersControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {
          |  addFilter(MembersController, "/*")
          |
          |  def member = FactoryGirl(Member).create()
          |
          |  it should "show members" in {
          |    get("/admin/members") {
          |      status should equal(200)
          |    }
          |    get("/admin/members/") {
          |      status should equal(200)
          |    }
          |    get("/admin/members.json") {
          |      status should equal(200)
          |    }
          |    get("/admin/members.xml") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a member in detail" in {
          |    get(s"/admin/members/${member.id}") {
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${member.id}.xml") {
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${member.id}.json") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/admin/members/new") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a member" in {
          |    post(s"/admin/members", "name" -> "dummy","favorite_int_number" -> Int.MaxValue.toString(),"favorite_long_number" -> Long.MaxValue.toString(),"favorite_short_number" -> Short.MaxValue.toString(),"favorite_double_number" -> Double.MaxValue.toString(),"favorite_float_number" -> Float.MaxValue.toString(),"is_activated" -> "true","birthday" -> new LocalDate().toString("YYYY-MM-dd")) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "12345") {
          |      post(s"/admin/members", "name" -> "dummy","favorite_int_number" -> Int.MaxValue.toString(),"favorite_long_number" -> Long.MaxValue.toString(),"favorite_short_number" -> Short.MaxValue.toString(),"favorite_double_number" -> Double.MaxValue.toString(),"favorite_float_number" -> Float.MaxValue.toString(),"is_activated" -> "true","birthday" -> new LocalDate().toString("YYYY-MM-dd"), "csrf-token" -> "12345") {
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        Member.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/admin/members/${member.id}/edit") {
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a member" in {
          |    put(s"/admin/members/${member.id}", "name" -> "dummy","favorite_int_number" -> Int.MaxValue.toString(),"favorite_long_number" -> Long.MaxValue.toString(),"favorite_short_number" -> Short.MaxValue.toString(),"favorite_double_number" -> Double.MaxValue.toString(),"favorite_float_number" -> Float.MaxValue.toString(),"is_activated" -> "true","birthday" -> new LocalDate().toString("YYYY-MM-dd")) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "12345") {
          |      put(s"/admin/members/${member.id}", "name" -> "dummy","favorite_int_number" -> Int.MaxValue.toString(),"favorite_long_number" -> Long.MaxValue.toString(),"favorite_short_number" -> Short.MaxValue.toString(),"favorite_double_number" -> Double.MaxValue.toString(),"favorite_float_number" -> Float.MaxValue.toString(),"is_activated" -> "true","birthday" -> new LocalDate().toString("YYYY-MM-dd"), "csrf-token" -> "12345") {
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    val member = FactoryGirl(Member).create()
          |    delete(s"/admin/members/${member.id}") {
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "aaaaaa") {
          |      delete(s"/admin/members/${member.id}?csrf-token=aaaaaa") {
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
      val code = generator.messagesConfCode("groupMembers", "groupMember", Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """
          |groupMember {
          |  flash {
          |    created="The group member was created."
          |    updated="The group member was updated."
          |    deleted="The group member was deleted."
          |  }
          |  list="Group Members"
          |  detail="Group Member"
          |  edit="Edit Group Member"
          |  new="New Group Member"
          |  delete.confirm="Are you sure?"
          |  id="ID"
          |  name="Name"
          |  isActivated="Is Activated"
          |  birthday="Birthday"
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("db/migration/xxx.sql") {
    it("should be created as expected") {
      val code = generator.migrationSQL("members", "member", Seq(
        ScaffoldGeneratorArg("name", "String"),
        ScaffoldGeneratorArg("nickname", "Option[String]", Some("varchar(64)")),
        ScaffoldGeneratorArg("isActivated", "Boolean"),
        ScaffoldGeneratorArg("birthday", "Option[LocalDate]")
      ))

      val expected =
        """-- For H2 Database
          |create table members (
          |  id bigserial not null primary key,
          |  name varchar(512) not null,
          |  nickname varchar(64),
          |  is_activated boolean not null,
          |  birthday date,
          |  created_at timestamp not null,
          |  updated_at timestamp not null
          |)
          |""".stripMargin
      code should equal(expected)
    }
  }

}

