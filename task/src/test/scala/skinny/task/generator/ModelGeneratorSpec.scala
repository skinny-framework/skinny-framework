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
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[Member]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
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
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[ProjectMember]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
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
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[ProjectMember]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
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
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[Member]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
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
          |import org.scalatest._
          |import scalikejdbc._
          |import scalikejdbc.scalatest._
          |import org.joda.time._
          |
          |class ProjectMemberSpec extends FlatSpec with Matchers with DBSettings with AutoRollback {
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("NoIdModel") {
    it("should be created as expected with tableName") {
      val generator = new ModelGenerator {
        override def withId = false
      }
      val code = generator.code(Seq("admin"), "noIdMember", Some("no_id_members"), Seq(
        "name" -> "String",
        "isActivated" -> "Boolean",
        "bytes" -> "ByteArray",
        "bytesOpt" -> "Option[ByteArray]",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected = """package model.admin
                       |
                       |import skinny.orm._, feature._
                       |import scalikejdbc._
                       |import org.joda.time._
                       |
                       |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
                       |case class NoIdMember(
                       |  name: String,
                       |  isActivated: Boolean,
                       |  bytes: Array[Byte],
                       |  bytesOpt: Option[Array[Byte]] = None,
                       |  birthday: Option[LocalDate] = None,
                       |  createdAt: DateTime,
                       |  updatedAt: DateTime
                       |)
                       |
                       |object NoIdMember extends SkinnyNoIdCRUDMapper[NoIdMember] with TimestampsFeature[NoIdMember] {
                       |  override lazy val tableName = "no_id_members"
                       |  override lazy val defaultAlias = createAlias("nim")
                       |
                       |  /*
                       |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
                       |   * (e.g.)
                       |   * override def extract(rs: WrappedResultSet, rn: ResultName[NoIdMember]) = autoConstruct(rs, rn)
                       |   *
                       |   * Be aware of excluding associations like this:
                       |   * (e.g.)
                       |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
                       |   * object Member extends SkinnyCRUDMapper[Member] {
                       |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
                       |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
                       |   * }
                       |   */
                       |  override def extract(rs: WrappedResultSet, rn: ResultName[NoIdMember]): NoIdMember = new NoIdMember(
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
        override def withId = false
        override def withTimestamps = false
      }
      val code = generator.code(Nil, "noIdProjectMember", None, Seq(
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
          |case class NoIdProjectMember(
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None
          |)
          |
          |object NoIdProjectMember extends SkinnyNoIdCRUDMapper[NoIdProjectMember] {
          |
          |  override lazy val defaultAlias = createAlias("nipm")
          |
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[NoIdProjectMember]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
          |  override def extract(rs: WrappedResultSet, rn: ResultName[NoIdProjectMember]): NoIdProjectMember = new NoIdProjectMember(
          |    name = rs.get(rn.name),
          |    isActivated = rs.get(rn.isActivated),
          |    birthday = rs.get(rn.birthday)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected without timestamps") {
      val generator = new ModelGenerator {
        override def withId = false
      }
      val code = generator.code(Seq("admin"), "noIdProjectMember", None, Seq(
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
          |case class NoIdProjectMember(
          |  name: String,
          |  isActivated: Boolean,
          |  birthday: Option[LocalDate] = None,
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object NoIdProjectMember extends SkinnyNoIdCRUDMapper[NoIdProjectMember] with TimestampsFeature[NoIdProjectMember] {
          |
          |  override lazy val defaultAlias = createAlias("nipm")
          |
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[NoIdProjectMember]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
          |  override def extract(rs: WrappedResultSet, rn: ResultName[NoIdProjectMember]): NoIdProjectMember = new NoIdProjectMember(
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
      val generator = new ModelGenerator {
        override def withId = false
      }
      val code = generator.code(Seq("admin"), "noIdMember", None, Seq())
      val expected =
        """package model.admin
          |
          |import skinny.orm._, feature._
          |import scalikejdbc._
          |import org.joda.time._
          |
          |// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
          |case class NoIdMember(
          |  createdAt: DateTime,
          |  updatedAt: DateTime
          |)
          |
          |object NoIdMember extends SkinnyNoIdCRUDMapper[NoIdMember] with TimestampsFeature[NoIdMember] {
          |
          |  override lazy val defaultAlias = createAlias("nim")
          |
          |  /*
          |   * If you're familiar with ScalikeJDBC/Skiny ORM, using #autoConstruct makes your mapper simpler.
          |   * (e.g.)
          |   * override def extract(rs: WrappedResultSet, rn: ResultName[NoIdMember]) = autoConstruct(rs, rn)
          |   *
          |   * Be aware of excluding associations like this:
          |   * (e.g.)
          |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
          |   * object Member extends SkinnyCRUDMapper[Member] {
          |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
          |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
          |   * }
          |   */
          |  override def extract(rs: WrappedResultSet, rn: ResultName[NoIdMember]): NoIdMember = new NoIdMember(
          |    createdAt = rs.get(rn.createdAt),
          |    updatedAt = rs.get(rn.updatedAt)
          |  )
          |}
          |""".stripMargin
      code should equal(expected)
    }
  }

}
