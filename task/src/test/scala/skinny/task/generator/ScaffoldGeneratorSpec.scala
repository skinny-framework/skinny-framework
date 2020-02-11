package skinny.task.generator

import org.scalatest._

class ScaffoldGeneratorSpec extends FunSpec with Matchers {

  val generator = ScaffoldSspGenerator

  describe("Controller (SkinnyResource)") {
    it("should be created as expected") {
      val code = generator.controllerCode(
        Seq("admin"),
        "members",
        "member",
        "ssp",
        Seq(
          ScaffoldGeneratorArg("name", "String", None),
          ScaffoldGeneratorArg("favoriteIntNumber", "Int", None),
          ScaffoldGeneratorArg("favoriteLongNumber", "Long", None),
          ScaffoldGeneratorArg("favoriteShortNumber", "Short", None),
          ScaffoldGeneratorArg("favoriteDoubleNumber", "Double", None),
          ScaffoldGeneratorArg("favoriteFloatNumber", "Float", None),
          ScaffoldGeneratorArg("magicNumber", "Option[Int]", None),
          ScaffoldGeneratorArg("isActivated", "Boolean", None),
          ScaffoldGeneratorArg("bytes", "Option[ByteArray]", None),
          ScaffoldGeneratorArg("birthday", "Option[LocalDate]", None),
          ScaffoldGeneratorArg("boss", "Option[Member]", None),
          ScaffoldGeneratorArg("friends", "Seq[Member]", None),
          ScaffoldGeneratorArg("timeToWakeUp", "Option[LocalTime]", None),
          ScaffoldGeneratorArg("createdAt", "DateTime", None)
        )
      )

      val expected =
        """package controller.admin
          |
          |import skinny._
          |import skinny.validator._
          |import _root_.controller._
          |import model.admin.Member
          |
          |class MembersController extends SkinnyResource with ApplicationController {
          |  protectFromForgery()
          |
          |  override def model = Member
          |  override def resourcesName = "members"
          |  override def resourceName = "member"
          |  override def messageResourceName = "adminMember"
          |
          |  override def resourcesBasePath = s"/admin/${toSnakeCase(resourcesName)}"
          |  override def useSnakeCasedParamKeys = true
          |
          |  override def viewsDirectoryPath = s"/admin/${resourcesName}"
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
          |    "bytes" -> ParamType.ByteArray,
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
          |    "bytes" -> ParamType.ByteArray,
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
    it("should be created as expected with namespaces") {
      val code = generator.controllerSpecCode(
        Seq("admin"),
        "members",
        "member",
        Seq(
          "name"                 -> "String",
          "favoriteIntNumber"    -> "Int",
          "favoriteLongNumber"   -> "Long",
          "favoriteShortNumber"  -> "Short",
          "favoriteDoubleNumber" -> "Double",
          "favoriteFloatNumber"  -> "Float",
          "isActivated"          -> "Boolean",
          "birthday"             -> "Option[LocalDate]"
        )
      )
      // TODO
      println(code)
    }
  }

  describe("Integration Test Spec for Controller (SkinnyResource)") {
    it("should be created as expected without namespaces") {
      val code = generator.integrationSpecCode(Nil,
                                               "members",
                                               "member",
                                               Seq(
                                                 "name" -> "String"
                                               ))

      val expected =
        """package integrationtest
          |
          |import org.scalatest._
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |import _root_.controller.Controllers
          |import model._
          |
          |class MembersController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
          |  addFilter(Controllers.members, "/*")
          |
          |  override def afterAll(): Unit = {
          |    super.afterAll()
          |    Member.deleteAll()
          |  }
          |
          |  def newMember = FactoryGirl(Member).create()
          |
          |  it should "show members" in {
          |    get("/members") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/members/") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/members.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/members.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a member in detail" in {
          |    get(s"/members/${newMember.id}") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/members/${newMember.id}.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/members/${newMember.id}.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/members/new") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a member" in {
          |    post(s"/members",
          |      "name" -> "dummy") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      post(s"/members",
          |        "name" -> "dummy",
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        Member.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/members/${newMember.id}/edit") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a member" in {
          |    put(s"/members/${newMember.id}",
          |      "name" -> "dummy") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      put(s"/members/${newMember.id}",
          |        "name" -> "dummy",
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    delete(s"/members/${newMember.id}") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "valid_token") {
          |      delete(s"/members/${newMember.id}?csrf-token=valid_token") {
          |        logBodyUnless(200)
          |        status should equal(200)
          |      }
          |    }
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected") {
      val code = generator.integrationSpecCode(
        Seq("admin"),
        "members",
        "member",
        Seq(
          "name"                 -> "String",
          "favoriteIntNumber"    -> "Int",
          "favoriteLongNumber"   -> "Long",
          "favoriteShortNumber"  -> "Short",
          "favoriteDoubleNumber" -> "Double",
          "favoriteFloatNumber"  -> "Float",
          "isActivated"          -> "Boolean",
          "birthday"             -> "Option[LocalDate]"
        )
      )

      val expected =
        """package integrationtest.admin
          |
          |import org.scalatest._
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |import _root_.controller.Controllers
          |import model.admin._
          |
          |class MembersController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
          |  addFilter(Controllers.adminMembers, "/*")
          |
          |  override def afterAll(): Unit = {
          |    super.afterAll()
          |    Member.deleteAll()
          |  }
          |
          |  def newMember = FactoryGirl(Member, "adminMember").create()
          |
          |  it should "show members" in {
          |    get("/admin/members") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members/") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a member in detail" in {
          |    get(s"/admin/members/${newMember.id}") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${newMember.id}.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${newMember.id}.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/admin/members/new") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a member" in {
          |    post(s"/admin/members",
          |      "name" -> "dummy",
          |      "favorite_int_number" -> Int.MaxValue.toString(),
          |      "favorite_long_number" -> Long.MaxValue.toString(),
          |      "favorite_short_number" -> Short.MaxValue.toString(),
          |      "favorite_double_number" -> Double.MaxValue.toString(),
          |      "favorite_float_number" -> Float.MaxValue.toString(),
          |      "is_activated" -> "true",
          |      "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate())) {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      post(s"/admin/members",
          |        "name" -> "dummy",
          |        "favorite_int_number" -> Int.MaxValue.toString(),
          |        "favorite_long_number" -> Long.MaxValue.toString(),
          |        "favorite_short_number" -> Short.MaxValue.toString(),
          |        "favorite_double_number" -> Double.MaxValue.toString(),
          |        "favorite_float_number" -> Float.MaxValue.toString(),
          |        "is_activated" -> "true",
          |        "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        Member.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/admin/members/${newMember.id}/edit") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a member" in {
          |    put(s"/admin/members/${newMember.id}",
          |      "name" -> "dummy",
          |      "favorite_int_number" -> Int.MaxValue.toString(),
          |      "favorite_long_number" -> Long.MaxValue.toString(),
          |      "favorite_short_number" -> Short.MaxValue.toString(),
          |      "favorite_double_number" -> Double.MaxValue.toString(),
          |      "favorite_float_number" -> Float.MaxValue.toString(),
          |      "is_activated" -> "true",
          |      "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate())) {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      put(s"/admin/members/${newMember.id}",
          |        "name" -> "dummy",
          |        "favorite_int_number" -> Int.MaxValue.toString(),
          |        "favorite_long_number" -> Long.MaxValue.toString(),
          |        "favorite_short_number" -> Short.MaxValue.toString(),
          |        "favorite_double_number" -> Double.MaxValue.toString(),
          |        "favorite_float_number" -> Float.MaxValue.toString(),
          |        "is_activated" -> "true",
          |        "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    delete(s"/admin/members/${newMember.id}") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "valid_token") {
          |      delete(s"/admin/members/${newMember.id}?csrf-token=valid_token") {
          |        logBodyUnless(200)
          |        status should equal(200)
          |      }
          |    }
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created for groupMembers") {
      val code = generator.integrationSpecCode(Seq("admin"),
                                               "groupMembers",
                                               "groupMember",
                                               Seq(
                                                 "name"              -> "String",
                                                 "favoriteIntNumber" -> "Int",
                                                 "isActivated"       -> "Boolean",
                                                 "birthday"          -> "Option[LocalDate]"
                                               ))

      val expected =
        """package integrationtest.admin
          |
          |import org.scalatest._
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |import _root_.controller.Controllers
          |import model.admin._
          |
          |class GroupMembersController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
          |  addFilter(Controllers.adminGroupMembers, "/*")
          |
          |  override def afterAll(): Unit = {
          |    super.afterAll()
          |    GroupMember.deleteAll()
          |  }
          |
          |  def newGroupMember = FactoryGirl(GroupMember, "adminGroupMember").create()
          |
          |  it should "show group members" in {
          |    get("/admin/group_members") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/group_members/") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/group_members.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/group_members.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a group member in detail" in {
          |    get(s"/admin/group_members/${newGroupMember.id}") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/group_members/${newGroupMember.id}.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/group_members/${newGroupMember.id}.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/admin/group_members/new") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a group member" in {
          |    post(s"/admin/group_members",
          |      "name" -> "dummy",
          |      "favorite_int_number" -> Int.MaxValue.toString(),
          |      "is_activated" -> "true",
          |      "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate())) {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      post(s"/admin/group_members",
          |        "name" -> "dummy",
          |        "favorite_int_number" -> Int.MaxValue.toString(),
          |        "is_activated" -> "true",
          |        "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        GroupMember.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/admin/group_members/${newGroupMember.id}/edit") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a group member" in {
          |    put(s"/admin/group_members/${newGroupMember.id}",
          |      "name" -> "dummy",
          |      "favorite_int_number" -> Int.MaxValue.toString(),
          |      "is_activated" -> "true",
          |      "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate())) {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      put(s"/admin/group_members/${newGroupMember.id}",
          |        "name" -> "dummy",
          |        "favorite_int_number" -> Int.MaxValue.toString(),
          |        "is_activated" -> "true",
          |        "birthday" -> skinny.util.DateTimeUtil.toString(new LocalDate()),
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a group member" in {
          |    delete(s"/admin/group_members/${newGroupMember.id}") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "valid_token") {
          |      delete(s"/admin/group_members/${newGroupMember.id}?csrf-token=valid_token") {
          |        logBodyUnless(200)
          |        status should equal(200)
          |      }
          |    }
          |  }
          |
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created with ByteArray") {
      val code = generator.integrationSpecCode(Seq("admin"),
                                               "members",
                                               "member",
                                               Seq(
                                                 "name"     -> "String",
                                                 "bytes"    -> "ByteArray",
                                                 "bytesOpt" -> "Option[ByteArray]"
                                               ))

      val expected =
        """package integrationtest.admin
          |
          |import org.scalatest._
          |import skinny._
          |import skinny.test._
          |import org.joda.time._
          |import _root_.controller.Controllers
          |import model.admin._
          |
          |class MembersController_IntegrationTestSpec extends SkinnyFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
          |  addFilter(Controllers.adminMembers, "/*")
          |
          |  override def afterAll(): Unit = {
          |    super.afterAll()
          |    Member.deleteAll()
          |  }
          |
          |  def newMember = FactoryGirl(Member, "adminMember").create()
          |
          |  it should "show members" in {
          |    get("/admin/members") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members/") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get("/admin/members.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show a member in detail" in {
          |    get(s"/admin/members/${newMember.id}") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${newMember.id}.xml") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |    get(s"/admin/members/${newMember.id}.json") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "show new entry form" in {
          |    get(s"/admin/members/new") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "create a member" in {
          |    post(s"/admin/members",
          |      "name" -> "dummy",
          |      "bytes" -> "dummy",
          |      "bytes_opt" -> "dummy") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      post(s"/admin/members",
          |        "name" -> "dummy",
          |        "bytes" -> "dummy",
          |        "bytes_opt" -> "dummy",
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |        val id = header("Location").split("/").last.toLong
          |        Member.findById(id).isDefined should equal(true)
          |      }
          |    }
          |  }
          |
          |  it should "show the edit form" in {
          |    get(s"/admin/members/${newMember.id}/edit") {
          |      logBodyUnless(200)
          |      status should equal(200)
          |    }
          |  }
          |
          |  it should "update a member" in {
          |    put(s"/admin/members/${newMember.id}",
          |      "name" -> "dummy",
          |      "bytes" -> "dummy",
          |      "bytes_opt" -> "dummy") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |
          |    withSession("csrf-token" -> "valid_token") {
          |      put(s"/admin/members/${newMember.id}",
          |        "name" -> "dummy",
          |        "bytes" -> "dummy",
          |        "bytes_opt" -> "dummy",
          |        "csrf-token" -> "valid_token") {
          |        logBodyUnless(302)
          |        status should equal(302)
          |      }
          |    }
          |  }
          |
          |  it should "delete a member" in {
          |    delete(s"/admin/members/${newMember.id}") {
          |      logBodyUnless(403)
          |      status should equal(403)
          |    }
          |    withSession("csrf-token" -> "valid_token") {
          |      delete(s"/admin/members/${newMember.id}?csrf-token=valid_token") {
          |        logBodyUnless(200)
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
      val code = generator.messagesConfCode("groupMember",
                                            "groupMembers",
                                            "groupMember",
                                            Seq(
                                              "name"        -> "String",
                                              "isActivated" -> "Boolean",
                                              "birthday"    -> "Option[LocalDate]"
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
      val code = generator.migrationSQL(
        "members",
        "member",
        Seq(
          ScaffoldGeneratorArg("name", "String"),
          ScaffoldGeneratorArg("nickname", "Option[String]", Some("varchar(64)")),
          ScaffoldGeneratorArg("isActivated", "Boolean"),
          ScaffoldGeneratorArg("birthday", "Option[LocalDate]")
        )
      )

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

  describe("toControllerName") {
    it("should work as expected without namespaces") {
      val name = generator.toControllerName(Nil, "projectMembers")
      name should equal("projectMembers")
    }
    it("should work as expected with namespaces") {
      val name = generator.toControllerName(Seq("admin", "foo", "barBaz"), "projectMembers")
      name should equal("adminFooBarBazProjectMembers")
    }
  }

}
