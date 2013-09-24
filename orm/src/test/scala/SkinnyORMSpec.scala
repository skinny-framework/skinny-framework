import skinny.orm._
import scalikejdbc._, SQLInterpolation._

import org.joda.time.DateTime

import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture.FunSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.orm.feature._

class SkinnyORMSpec extends FunSpec with ShouldMatchers
    with Connection
    with Formatter
    with CreateTables
    with AutoRollback {

  override def fixture(implicit session: DBSession) {

    // #createWithNamedValues
    val countryId1 = Country.withColumns { c => Country.createWithNamedValues(c.name -> "Japan") }
    val countryId2 = Country.withColumns { c => Country.createWithNamedValues(c.name -> "China") }

    // #withColumns
    val groupId1 = Group.withColumns { g =>
      Group.createWithNamedValues(g.name -> "Scala Users Group")
    }
    val groupId2 = Group.withColumns { g =>
      Group.createWithNamedValues(g.name -> "Java Community")
    }
    val groupId3 = Group.withColumns { g =>
      Group.createWithNamedValues(g.name -> "PHP Users")
    }
    Group.deleteById(groupId3)
    Group.countAll() should equal(2L)

    val companyId = Company.withColumns(c => Company.createWithNamedValues(c.name -> "Typesafe"))

    val companyId2 = Company.withColumns(c => Company.createWithNamedValues(c.name -> "Oracle"))
    Company.deleteById(companyId2)
    Company.countAll() should equal(1L)

    Member.withColumns { m =>

      val alice = Member.createWithNamedValues(
        m.countryId -> countryId1,
        m.companyId -> companyId,
        m.createdAt -> DateTime.now
      )
      val bob = Member.createWithNamedValues(
        m.countryId -> countryId2,
        m.mentorId -> alice,
        m.createdAt -> DateTime.now
      )
      val chris = Member.createWithNamedValues(
        m.countryId -> countryId1,
        m.mentorId -> alice,
        m.createdAt -> DateTime.now
      )
      Name.withColumns { mn =>
        Name.createWithNamedValues(mn.memberId -> alice, mn.first -> "Alice", mn.last -> "Cooper")
        Name.createWithNamedValues(mn.memberId -> bob, mn.first -> "Bob", mn.last -> "Marley")
        Name.createWithNamedValues(mn.memberId -> chris, mn.first -> "Chris", mn.last -> "Birchall")
      }

      GroupMember.withColumns { gm =>
        GroupMember.createWithNamedValues(gm.memberId -> alice, gm.groupId -> groupId1)
        GroupMember.createWithNamedValues(gm.memberId -> bob, gm.groupId -> groupId1)
        GroupMember.createWithNamedValues(gm.memberId -> bob, gm.groupId -> groupId2)
      }

      Skill.withColumns { s =>
        val skillId = Skill.createWithNamedValues(s.name -> "Programming")
        Skill.updateById(skillId).withNamedValues(s.name -> "Web development")

        MemberSkill.withColumns(ms =>
          MemberSkill.createWithNamedValues(ms.memberId -> alice, ms.skillId -> skillId))
      }
    }

  }

  describe("Operations") {

    it("should have #findById(Long)") { implicit session =>
      val memberIds = Member.findAll().map(_.id)
      val member = Member.findById(memberIds.head)
      member.isDefined should be(true)

      val nonExistingId = (1 to 10000).find(i => !memberIds.contains(i)).get
      val none = Member.findById(nonExistingId)
      none.isDefined should be(false)
    }

    it("should have #findAll()") { implicit session =>
      Member.findAll().size should be > (0)
    }

    it("should have #countAll()") { implicit session =>
      Member.countAll() should be > (0L)
    }

    it("should have #findAllBy(SQLSyntax, Int, Int)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      Member.withAlias { m =>
        Member.findAllBy(sqls.eq(m.countryId, countryId)).size should be > (0)
        Member.findAllBy(sqls.eq(m.countryId, countryId), 10, 0).size should be > (0)
        Member.findAllBy(sqls.eq(m.countryId, countryId), 1, 0).size should equal(1)
      }
    }

    it("should have #countBy(SQLSyntax)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      Member.withAlias(s =>
        Member.countBy(sqls.eq(s.countryId, countryId))
      ) should be > (0L)
    }

    it("should have #updateById(Long)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      val memberId = Member.withColumns(c => Member.createWithNamedValues(
        c.countryId -> countryId, c.createdAt -> DateTime.now
      ))
      val mentorId = Member.findAll().head.id
      Member.withColumns { m =>
        Member.updateById(memberId).withNamedValues(m.mentorId -> mentorId)
      }
      val updated = Member.findById(memberId)
      updated.get.mentorId should equal(Some(mentorId))
    }

    it("should have #deleteById(Long)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      val memberId = Member.withColumns(m => Member.createWithNamedValues(
        m.countryId -> countryId, m.createdAt -> DateTime.now
      ))
      Member.deleteById(memberId)
    }
  }

  describe("Relationship") {

    it("should have #belongsTo, #hasOne") { implicit session =>
      Member.withAlias { m =>

        val members = Member.findAll()
        members.size should be > (0)

        val alice = members.filter(_.name.get.first == "Alice").head
        alice.mentor.isDefined should be(false)
        alice.country.name should equal("Japan")

        val bob = members.filter(_.name.get.first == "Bob").head
        bob.mentor.isDefined should be(true)
        bob.country.name should equal("China")
      }
    }

    it("should have #hasMany") { implicit session =>
      Member.withAlias { m =>

        val members = Member.findAll()

        val withGroups = members.filter(_.name.get.first == "Bob").head
        withGroups.groups.size should equal(2)
        val withoutGroups = members.filter(_.name.get.first == "Chris").head
        withoutGroups.groups.size should equal(0)

        val alice = members.filter(_.name.get.first == "Alice").head
        alice.mentorees.size should equal(2)
        val bob = members.filter(_.name.get.first == "Bob").head
        bob.mentorees.size should equal(0)

        {
          // skills should be empty
          val withSkills = members.filter(_.name.get.first == "Alice").head
          withSkills.skills.size should equal(0)
          val withoutSkills = members.filter(_.name.get.first == "Chris").head
          withoutSkills.skills.size should equal(0)
        }

        {
          val membersWithSkills = Member.joins(Member.skills).findAll()
          val withSkills = membersWithSkills.filter(_.name.get.first == "Alice").head
          withSkills.skills.size should equal(1)
          val withoutSkills = membersWithSkills.filter(_.name.get.first == "Chris").head
          withoutSkills.skills.size should equal(0)
        }

      }
    }
  }

}

// ------------------------
// examples
// ------------------------

case class Member(
  id: Long,
  name: Option[Name] = None,
  countryId: Long,
  mentorId: Option[Long],
  companyId: Option[Long],
  createdAt: DateTime,
  country: Country,
  company: Option[Company] = None,
  mentor: Option[Member] = None,
  mentorees: Seq[Member] = Nil,
  groups: Seq[Group] = Nil,
  skills: Seq[Skill] = Nil)

object Member extends SkinnyCRUDMapper[Member] {
  override val tableName = "members"
  override val defaultAlias = createAlias("m")
  val mentorAlias = createAlias("mentor")
  val mentoreeAlias = createAlias("mentoree")

  // if you use hasOne, joined entity should be Option[Entity]
  innerJoinWithDefaults(Country, (m, c) => sqls.eq(m.countryId, c.id)).byDefaultEvenIfAssociated

  // one-to-one
  belongsTo[Company](Company, (m, c) => m.copy(company = c)).byDefault
  belongsToWithAlias[Member](Member -> Member.mentorAlias, (m, mentor) => m.copy(mentor = mentor)).byDefault
  hasOne[Name](Name, (m, name) => m.copy(name = name)).byDefault

  // groups
  hasManyThrough[Group](
    GroupMember, Group, (member, groups) => member.copy(groups = groups)
  ).byDefault

  // skills
  val skills = hasManyThrough[Skill](
    MemberSkill, Skill, (member, skills) => member.copy(skills = skills)
  )

  // mentorees
  hasMany[Member](
    many = Member -> Member.mentoreeAlias,
    on = (m, mentorees) => sqls.eq(m.id, mentorees.mentorId),
    merge = (member, mentorees) => member.copy(mentorees = mentorees)
  ).byDefault

  override def extract(rs: WrappedResultSet, n: ResultName[Member]): Member = new Member(
    id = rs.long(n.id),
    countryId = rs.long(n.countryId),
    companyId = rs.longOpt(n.companyId),
    mentorId = rs.longOpt(n.mentorId),
    createdAt = rs.timestamp(n.createdAt).toDateTime,
    country = Country(rs)
  )
}

case class Name(memberId: Long, first: String, last: String, member: Option[Member] = None)
object Name extends SkinnyCRUDMapper[Name] {
  override val tableName = "names"
  override val defaultAlias = createAlias("nm")
  override val useAutoIncrementPrimaryKey = false

  val member = belongsTo[Member](Member, (name, member) => name.copy(member = member)).byDefault

  def extract(rs: WrappedResultSet, s: ResultName[Name]): Name = new Name(
    memberId = rs.long(s.memberId),
    first = rs.string(s.first),
    last = rs.string(s.last)
  )
}

case class Company(id: Long, name: String)
object Company extends SkinnyCRUDMapper[Company] with SoftDeleteWithBooleanFeature[Company] {
  override val tableName = "companies"
  override val defaultAlias = createAlias("cmp")
  def extract(rs: WrappedResultSet, s: ResultName[Company]): Company = new Company(
    id = rs.long(s.id),
    name = rs.string(s.name)
  )
}

case class Country(id: Long, name: String)
object Country extends SkinnyCRUDMapper[Country] {
  override val tableName = "countries"
  override val defaultAlias = createAlias("cnt")
  def extract(rs: WrappedResultSet, s: ResultName[Country]): Country = new Country(
    id = rs.long(s.id), name = rs.string(s.name)
  )
}

case class Group(id: Long, name: String)
object Group extends SkinnyCRUDMapper[Group] with SoftDeleteWithTimestampFeature[Group] {
  override val tableName = "groups"
  override val defaultAlias = createAlias("g")
  def extract(rs: WrappedResultSet, s: ResultName[Group]): Group = new Group(
    id = rs.long(s.id),
    name = rs.string(s.name)
  )
}

case class GroupMember(groupId: Long, memberId: Long)
object GroupMember extends SkinnyJoinTable[GroupMember] {
  override val tableName = "groups_members"
  override val defaultAlias = createAlias("gm")
}

case class Skill(id: Long, name: String, createdAt: DateTime, updatedAt: Option[DateTime] = None)
object Skill extends SkinnyCRUDMapper[Skill] with TimestampsFeature[Skill] {
  override val tableName = "skills"
  override val defaultAlias = createAlias("s")
  def extract(rs: WrappedResultSet, s: ResultName[Skill]): Skill = new Skill(
    id = rs.long(s.id),
    name = rs.string(s.name),
    createdAt = rs.timestamp(s.createdAt).toDateTime,
    updatedAt = rs.timestampOpt(s.updatedAt).map(_.toDateTime)
  )
}

case class MemberSkill(memberId: Long, skillId: Long)
object MemberSkill extends SkinnyJoinTable[MemberSkill] {
  override val tableName = "members_skills"
  override val defaultAlias = createAlias("ms")
}

// ------------------------
// common settings
// ------------------------

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:mem:skinny-mapper-test", "sa", "sa")
}

trait Formatter {
  GlobalSettings.sqlFormatter = SQLFormatterSettings("skinny.mapper.formatter.HibernateSQLFormatter")
}

trait CreateTables { self: Connection =>

  DB autoCommit { implicit s =>
    sql"""
create table members (
  id bigint auto_increment primary key not null,
  country_id bigint not null,
  company_id bigint,
  mentor_id bigint,
  created_at timestamp not null
);
create table names (
  member_id bigint primary key not null,
  first varchar(64) not null,
  last varchar(64) not null
);
create table countries (
  id bigint auto_increment primary key not null,
  name varchar(255) not null
);
create table companies (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  is_deleted boolean default false not null
);
create table groups (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  deleted_at timestamp
);
create table groups_members (
  group_id bigint not null,
  member_id bigint not null
);
create table skills (
  id bigint auto_increment primary key not null,
  name varchar(255) not null,
  created_at timestamp not null,
  updated_at timestamp
);
create table members_skills (
  member_id bigint not null,
  skill_id bigint not null
);
    """.execute.apply()
  }

}
