package skinny.task.generator

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class ModelGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ModelGenerator

  describe("Model") {
    it("should be created as expected with tableName") {
      val code = generator.code("member", Some("members"), Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._, SQLInterpolation._
          |import org.joda.time._
          |
          |case class Member(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: Option[DateTime] = None
          |)
          |
          |object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member] {
          |  override val tableName = "members"
          |  override val defaultAlias = createAlias("m")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = new Member(
          |    id = rs.long(rn.id),
          |    name = rs.string(rn.name),
          |    isActivated = rs.boolean(rn.isActivated),
          |    birthday = rs.localDateOpt(rn.birthday),
          |    createdAt = rs.dateTime(rn.createdAt),
          |    updatedAt = rs.dateTimeOpt(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected without tableName") {
      val code = generator.code("member", None, Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """package model
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._, SQLInterpolation._
          |import org.joda.time._
          |
          |case class Member(
          |  id: Long,
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: Option[DateTime] = None
          |)
          |
          |object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member] {
          |
          |  override val defaultAlias = createAlias("m")
          |
          |  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = new Member(
          |    id = rs.long(rn.id),
          |    name = rs.string(rn.name),
          |    isActivated = rs.boolean(rn.isActivated),
          |    birthday = rs.localDateOpt(rn.birthday),
          |    createdAt = rs.dateTime(rn.createdAt),
          |    updatedAt = rs.dateTimeOpt(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("Model") {
    it("should be created as expected") {
      val code = generator.spec("member")
      val expected =
        """package model
          |
          |import skinny.test._
          |import org.scalatest.fixture.FlatSpec
          |import scalikejdbc._, SQLInterpolation._
          |import scalikejdbc.scalatest._
          |import org.joda.time._
          |
          |class MemberSpec extends FlatSpec with AutoRollback {
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
