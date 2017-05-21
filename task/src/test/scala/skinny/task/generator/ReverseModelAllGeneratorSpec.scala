package skinny.task.generator

import java.io.File
import java.nio.charset.Charset

import org.apache.commons.io.FileUtils
import org.scalatest._
import scalikejdbc._
import skinny.{ DBSettings, SkinnyEnv }

class ReverseModelAllGeneratorSpec extends FunSpec with Matchers {

  val generator = new ReverseModelAllGenerator {
    override def sourceDir       = "tmp/ReverseModelAllGeneratorSpec/src/main/scala"
    override def resourceDir     = "tmp/ReverseModelAllGeneratorSpec/src/main/resources"
    override def testSourceDir   = "tmp/ReverseModelAllGeneratorSpec/src/test/scala"
    override def testResourceDir = "tmp/ReverseModelAllGeneratorSpec/src/test/resources"
  }

  describe("ReverseModelAllGenerator") {
    it("should be created as expected") {
      System.setProperty(SkinnyEnv.PropertyKey, "test")
      DBSettings.initialize()
      DB.localTx { implicit s =>
        sql"""
create table company (
  id bigserial not null primary key,
  name varchar(100) not null,
  url varchar(512),
  created_at timestamp not null default current_timestamp
);

create table member (
  id bigserial not null primary key,
  name varchar(512) not null,
  nickname varchar(32),
  company_id bigint references company(id),
  joined_at timestamp not null,
  leave_at timestamp
);
""".execute.apply()
      }

      FileUtils.deleteDirectory(new File("tmp/ReverseModelAllGeneratorSpec"))
      generator.run(List(SkinnyEnv.getOrDevelopment()))

      val company = FileUtils.readFileToString(
        new File("tmp/ReverseModelAllGeneratorSpec/src/main/scala/model/Company.scala"),
        Charset.defaultCharset()
      )
      company should equal(
        s"""package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Company(
  id: Long,
  name: String,
  url: Option[String] = None,
  createdAt: DateTime,
  members: Seq[Member] = Nil
)

object Company extends SkinnyCRUDMapper[Company] {
  override lazy val tableName = "company"
  override lazy val defaultAlias = createAlias("c")

  lazy val membersRef = hasMany[Member](
    many = Member -> Member.defaultAlias,
    on = (c, m) => sqls.eq(c.id, m.companyId),
    merge = (c, ms) => c.copy(members = ms)
  )

  override def extract(rs: WrappedResultSet, rn: ResultName[Company]): Company = {
    autoConstruct(rs, rn, "members")
  }
}
""".stripMargin
      )

      val member = FileUtils.readFileToString(
        new File("tmp/ReverseModelAllGeneratorSpec/src/main/scala/model/Member.scala"),
        Charset.defaultCharset()
      )
      member should equal(
        s"""package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Member(
  id: Long,
  name: String,
  nickname: Option[String] = None,
  companyId: Option[Long] = None,
  joinedAt: DateTime,
  leaveAt: Option[DateTime] = None,
  company: Option[Company] = None
)

object Member extends SkinnyCRUDMapper[Member] {
  override lazy val tableName = "member"
  override lazy val defaultAlias = createAlias("m")

  lazy val companyRef = belongsTo[Company](Company, (m, c) => m.copy(company = c))

  override def extract(rs: WrappedResultSet, rn: ResultName[Member]): Member = {
    autoConstruct(rs, rn, "company")
  }
}
""".stripMargin
      )

      val companySpec = FileUtils.readFileToString(
        new File("tmp/ReverseModelAllGeneratorSpec/src/test/scala/model/CompanySpec.scala"),
        Charset.defaultCharset()
      )
      companySpec should equal(
        s"""package model

import skinny.DBSettings
import skinny.test._
import org.scalatest.fixture.FlatSpec
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest._
import org.joda.time._

class CompanySpec extends FlatSpec with Matchers with DBSettings with AutoRollback {
}
"""
      )

      val memberSpec = FileUtils.readFileToString(
        new File("tmp/ReverseModelAllGeneratorSpec/src/test/scala/model/MemberSpec.scala"),
        Charset.defaultCharset()
      )
      memberSpec should equal(
        s"""package model

import skinny.DBSettings
import skinny.test._
import org.scalatest.fixture.FlatSpec
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest._
import org.joda.time._

class MemberSpec extends FlatSpec with Matchers with DBSettings with AutoRollback {
}
"""
      )
    }
  }

}
