package skinny.task.generator

import org.scalatest._

class ModelGeneratorSpec extends FunSpec with Matchers {

  val generator = ModelGenerator

  describe("Model") {
    it("should be created as expected with tableName") {
      val code = generator.code(Seq("admin"), "member", Some("members"), Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "bytes" -> "ByteArray",
        "bytesOpt" -> "Option[ByteArray]",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class Member(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  bytes: Array[Byte],
          |  bytesOpt: Option[Array[Byte]] = None,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member] {
          |  override lazy val tableName = "members"
          |  override lazy val defaultAlias = createAlias("m")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = new Member(
          |    id = rs.get(rn.id),
          |    name = rs.get(rn.name),
          |    isActivated = rs.get(rn.isActivated),
          |    bytes = rs.get(rn.bytes),
          |    bytesOpt = rs.get(rn.bytesOpt),
          |    birthday = rs.get(rn.birthday),
          |    createdAt = rs.get(rn.createdAt),
          |    updatedAt = rs.get(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected without tableName") {
      val generator = new ModelGenerator {
        override def withTimestamps = false
      }
      val code = generator.code(Nil, "projectMember", None, Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class ProjectMember(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None
          |)
          |
          |object ProjectMember extends SkinnyCRUDMapper[ProjectMember] {
          |
          |  override lazy val defaultAlias = createAlias("pm")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[ProjectMember]): ProjectMember = new ProjectMember(
          |    id = rs.get(rn.id),
          |    name = rs.get(rn.name),
          |    isActivated = rs.get(rn.isActivated),
          |    birthday = rs.get(rn.birthday)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected without timestamps") {
      val code = generator.code(Seq("admin"), "projectMember", None, Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class ProjectMember(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object ProjectMember extends SkinnyCRUDMapper[ProjectMember] with TimestampsFeature[ProjectMember] {
          |
          |  override lazy val defaultAlias = createAlias("pm")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[ProjectMember]): ProjectMember = new ProjectMember(
          |    id = rs.get(rn.id),
          |    name = rs.get(rn.name),
          |    isActivated = rs.get(rn.isActivated),
          |    birthday = rs.get(rn.birthday),
          |    createdAt = rs.get(rn.createdAt),
          |    updatedAt = rs.get(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected without attributes") {
      val code = generator.code(Seq("admin"), "member", None, Seq())
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class Member(
          |  id: Long,
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member] {
          |
          |  override lazy val defaultAlias = createAlias("m")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = new Member(
          |    id = rs.get(rn.id),
          |    createdAt = rs.get(rn.createdAt),
          |    updatedAt = rs.get(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("Model") {
    it("should be created as expected") {
      val code = generator.spec(Seq("admin"), "projectMember")
      val expected =
        """package model.admin
          |
          |import skinny.DBSettings
          |import skinny.test._
          |import org.scalatest.fixture.FlatSpec
          |import scalikejdbc._
          |import scalikejdbc.scalatest._
          |import org.joda.time._
          |
          |class ProjectMemberSpec extends FlatSpec with DBSettings with AutoRollback {
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
