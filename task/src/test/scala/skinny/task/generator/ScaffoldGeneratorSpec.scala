package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldSspGenerator

  describe("Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerCode("members", "member", "ssp", Seq(
        "name" -> "String",
        "favoriteIntNumber" -> "Int",
        "favoriteLongNumber" -> "Long",
        "favoriteShortNumber" -> "Short",
        "favoriteDoubleNumber" -> "Double",
        "favoriteFloatNumber" -> "Float",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]",
        "timeToWakeUp" -> "Option[LocalTime]",
        "createdAt" -> "DateTime"
      ))

      val expected =
        """package controller
          |
          |import skinny._
          |import skinny.validator._
          |import model.Member
          |
          |object MembersController extends SkinnyResource with ApplicationController {
          |  protectFromForgery()
          |
          |  override def model = Member
          |  override def resourcesName = "members"
          |  override def resourceName = "member"
          |
          |  override def createForm = validation(createParams,
          |    paramKey("name") is required & maxLength(512),
          |    paramKey("favoriteIntNumber") is required & numeric & intValue,
          |    paramKey("favoriteLongNumber") is required & numeric & longValue,
          |    paramKey("favoriteShortNumber") is required & numeric & intValue,
          |    paramKey("favoriteDoubleNumber") is required & doubleValue,
          |    paramKey("favoriteFloatNumber") is required & floatValue,
          |    paramKey("magicNumber") is numeric & intValue,
          |    paramKey("birthday") is dateFormat,
          |    paramKey("timeToWakeUp") is timeFormat,
          |    paramKey("createdAt") is required & dateTimeFormat
          |  )
          |  override def createParams = Params(params)
          |    .withDate("birthday")
          |    .withTime("timeToWakeUp")
          |    .withDateTime("createdAt")
          |  override def createFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favoriteIntNumber" -> ParamType.Int,
          |    "favoriteLongNumber" -> ParamType.Long,
          |    "favoriteShortNumber" -> ParamType.Short,
          |    "favoriteDoubleNumber" -> ParamType.Double,
          |    "favoriteFloatNumber" -> ParamType.Float,
          |    "magicNumber" -> ParamType.Int,
          |    "isActivated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate,
          |    "timeToWakeUp" -> ParamType.LocalTime,
          |    "createdAt" -> ParamType.DateTime
          |  )
          |
          |  override def updateForm = validation(updateParams,
          |    paramKey("name") is required & maxLength(512),
          |    paramKey("favoriteIntNumber") is required & numeric & intValue,
          |    paramKey("favoriteLongNumber") is required & numeric & longValue,
          |    paramKey("favoriteShortNumber") is required & numeric & intValue,
          |    paramKey("favoriteDoubleNumber") is required & doubleValue,
          |    paramKey("favoriteFloatNumber") is required & floatValue,
          |    paramKey("magicNumber") is numeric & intValue,
          |    paramKey("birthday") is dateFormat,
          |    paramKey("timeToWakeUp") is timeFormat,
          |    paramKey("createdAt") is required & dateTimeFormat
          |  )
          |  override def updateParams = Params(params)
          |    .withDate("birthday")
          |    .withTime("timeToWakeUp")
          |    .withDateTime("createdAt")
          |  override def updateFormStrongParameters = Seq(
          |    "name" -> ParamType.String,
          |    "favoriteIntNumber" -> ParamType.Int,
          |    "favoriteLongNumber" -> ParamType.Long,
          |    "favoriteShortNumber" -> ParamType.Short,
          |    "favoriteDoubleNumber" -> ParamType.Double,
          |    "favoriteFloatNumber" -> ParamType.Float,
          |    "magicNumber" -> ParamType.Int,
          |    "isActivated" -> ParamType.Boolean,
          |    "birthday" -> ParamType.LocalDate,
          |    "timeToWakeUp" -> ParamType.LocalTime,
          |    "createdAt" -> ParamType.DateTime
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
        "favoriteIntNumber" -> "Int",
        "favoriteLongNumber" -> "Long",
        "favoriteShortNumber" -> "Short",
        "favoriteDoubleNumber" -> "Double",
        "favoriteFloatNumber" -> "Float",
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
          |    post(s"/members", "name" -> "dummy","favoriteIntNumber" -> Int.MaxValue.toString(),"favoriteLongNumber" -> Long.MaxValue.toString(),"favoriteShortNumber" -> Short.MaxValue.toString(),"favoriteDoubleNumber" -> Double.MaxValue.toString(),"favoriteFloatNumber" -> Float.MaxValue.toString(),"isActivated" -> "true","birthday" -> new LocalDate().toString()) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "12345") {
          |      post(s"/members", "name" -> "dummy","favoriteIntNumber" -> Int.MaxValue.toString(),"favoriteLongNumber" -> Long.MaxValue.toString(),"favoriteShortNumber" -> Short.MaxValue.toString(),"favoriteDoubleNumber" -> Double.MaxValue.toString(),"favoriteFloatNumber" -> Float.MaxValue.toString(),"isActivated" -> "true","birthday" -> new LocalDate().toString(), "csrf-token" -> "12345") {
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
          |    put(s"/members/${member.id}", "name" -> "dummy","favoriteIntNumber" -> Int.MaxValue.toString(),"favoriteLongNumber" -> Long.MaxValue.toString(),"favoriteShortNumber" -> Short.MaxValue.toString(),"favoriteDoubleNumber" -> Double.MaxValue.toString(),"favoriteFloatNumber" -> Float.MaxValue.toString(),"isActivated" -> "true","birthday" -> new LocalDate().toString()) {
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "12345") {
          |      put(s"/members/${member.id}", "name" -> "dummy","favoriteIntNumber" -> Int.MaxValue.toString(),"favoriteLongNumber" -> Long.MaxValue.toString(),"favoriteShortNumber" -> Short.MaxValue.toString(),"favoriteDoubleNumber" -> Double.MaxValue.toString(),"favoriteFloatNumber" -> Float.MaxValue.toString(),"isActivated" -> "true","birthday" -> new LocalDate().toString(), "csrf-token" -> "12345") {
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    val member = FactoryGirl(Member).create()
          |    delete(s"/members/${member.id}") {
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "aaaaaa") {
          |      delete(s"/members/${member.id}?csrf-token=aaaaaa") {
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
          |  updated_at timestamp
          |)
          |""".stripMargin
      code should equal(expected)
    }
  }

}
