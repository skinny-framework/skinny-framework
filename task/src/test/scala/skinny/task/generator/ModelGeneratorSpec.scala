package skinny.task.generator

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class ModelGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ModelGenerator

  describe("Model") {
    it("should be created as expected with tableName") {
      val code = generator.code(Seq("admin"), "member", Some("members"), Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._, SQLInterpolation._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class Member(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member] {
          |  override val tableName = "members"
          |  override val defaultAlias = createAlias("m")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = new Member(
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

    it("should be created as expected without tableName") {
      val code = generator.code(Seq("admin"), "projectMember", None, Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._, SQLInterpolation._
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
          |  override val defaultAlias = createAlias("pm")
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
          |import scalikejdbc._, SQLInterpolation._
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
          |  override val defaultAlias = createAlias("m")
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
          |import skinny.test._
          |import org.scalatest.fixture.FlatSpec
          |import scalikejdbc._, SQLInterpolation._
          |import scalikejdbc.scalatest._
          |import org.joda.time._
          |
          |class ProjectMemberSpec extends FlatSpec with AutoRollback {
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
