package skinny.orm

import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import skinny.orm.feature._
import org.joda.time.DateTime
import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import skinny.test.FactoryGirl
import skinny.orm.exception.OptimisticLockException
import ar.com.gonto.factorypal.objects.{ ObjectSetter, ObjectBuilder }

// --------------------------------
// Sample entities and mappers
// --------------------------------

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
    createdAt = rs.dateTime(n.createdAt),
    country = Country(rs)
  )
}

case class Name(memberId: Long, first: String, last: String, createdAt: DateTime, updatedAt: Option[DateTime] = None, member: Option[Member] = None)

object Name extends SkinnyCRUDMapper[Name]
    with TimestampsFeature[Name]
    with OptimisticLockWithTimestampFeature[Name] {

  override val tableName = "names"
  override val lockTimestampFieldName = "updatedAt"

  override val useAutoIncrementPrimaryKey = false
  override val primaryKeyName = "memberId"

  override val defaultAlias = createAlias("nm")

  val member = belongsTo[Member](Member, (name, member) => name.copy(member = member)).byDefault

  def extract(rs: WrappedResultSet, s: ResultName[Name]): Name = new Name(
    memberId = rs.long(s.memberId),
    first = rs.string(s.first),
    last = rs.string(s.last),
    createdAt = rs.dateTime(s.createdAt),
    updatedAt = rs.dateTimeOpt(s.updatedAt)
  )
}

case class Company(id: Option[Long] = None, name: String) extends MutableSkinnyRecord[Company] {
  def skinnyCRUDMapper = Company
}

object Company extends SkinnyCRUDMapper[Company] with SoftDeleteWithBooleanFeature[Company] {
  override val tableName = "companies"
  override val defaultAlias = createAlias("cmp")
  def extract(rs: WrappedResultSet, s: ResultName[Company]): Company = new Company(
    id = rs.longOpt(s.id),
    name = rs.string(s.name)
  )
}

case class Country(id: Long, name: String) extends SkinnyRecord[Country] {
  def skinnyCRUDMapper = Country
}

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

case class Skill(id: Long, name: String, createdAt: DateTime, updatedAt: Option[DateTime] = None, lockVersion: Long)

object Skill extends SkinnyCRUDMapper[Skill] with TimestampsFeature[Skill] with OptimisticLockWithVersionFeature[Skill] {
  override val tableName = "skills"
  override val defaultAlias = createAlias("s")
  def extract(rs: WrappedResultSet, s: ResultName[Skill]): Skill = new Skill(
    id = rs.long(s.id),
    name = rs.string(s.name),
    createdAt = rs.dateTime(s.createdAt),
    updatedAt = rs.dateTimeOpt(s.updatedAt),
    lockVersion = rs.long(s.lockVersion)
  )
}

case class MemberSkill(memberId: Long, skillId: Long)

object MemberSkill extends SkinnyJoinTable[MemberSkill] {
  override val tableName = "members_skills"
  override val defaultAlias = createAlias("ms")
}

// --------------------------------
// Actually working specification
// --------------------------------

class SkinnyORMSpec extends fixture.FunSpec with ShouldMatchers
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

    val companyId = Company.withColumns(c => Company.createWithNamedValues(c.name -> "Typesafe"))
    val companyId2 = Company.withColumns(c => Company.createWithNamedValues(c.name -> "Oracle"))

    Member.withColumns { m =>
      // Member doesn't use TimestampsFeature
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

  describe("MutableSkinnyRecord") {
    it("should act like ActiveRecord") { implicit session =>
      val companyId = Company(name = "Sun").create()
      val company = Company.findById(companyId).get

      company.copy(name = "Oracle").save()
      Company.findById(companyId).get.name should equal("Oracle")

      company.destroy()
      Company.findById(companyId) should equal(None)
    }

    /* TODO
[info]   java.lang.IllegalStateException: No builder register for None
[info]   at ar.com.gonto.factorypal.FactoryPal$$anonfun$1.apply(FactoryPal.scala:43)
[info]   at ar.com.gonto.factorypal.FactoryPal$$anonfun$1.apply(FactoryPal.scala:43)
[info]   at scala.Option.getOrElse(Option.scala:120)
[info]   at ar.com.gonto.factorypal.FactoryPal$.create(FactoryPal.scala:42)

    it("should work with FactoryPal") { implicit session =>
      import ar.com.gonto.factorypal.FactoryPal
      val companyByFactoryPal = FactoryPal.create[Company]() { company =>
        company.name.mapsTo("Sun")
      }
      val companyId = companyByFactoryPal.create()
      val company = Company.findById(companyId).get

      company.copy(name = "Oracle").save()
      Company.findById(companyId).get.name should equal("Oracle")

      company.destroy()
      Company.findById(companyId) should equal(None)
    }
    */
  }

  describe("SkinnyRecord") {
    it("should act like ActiveRecord") { implicit session =>
      val cnt = Country.column
      val countryId = Country.createWithNamedValues(cnt.name -> "Brazil")
      val country = Country.findById(countryId).get

      country.copy(name = "BRAZIL").save()
      Country.findById(countryId).get.name should equal("BRAZIL")

      country.destroy()
      Country.findById(countryId) should equal(None)
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

  describe("Optimistic Lock") {

    it("should update with lock version") { implicit session =>
      val s = Skill.column
      val skill = FactoryGirl(Skill).create()

      // with optimistic lock
      Skill.updateByIdAndVersion(skill.id, skill.lockVersion).withNamedValues(s.name -> "Java Programming")
      intercept[OptimisticLockException] {
        Skill.updateByIdAndVersion(skill.id, skill.lockVersion).withNamedValues(s.name -> "Ruby Programming")
      }
      // without lock
      Skill.updateById(skill.id).withNamedValues(s.name -> "Ruby Programming")
    }

    it("should delete with lock version") { implicit session =>
      val skill = FactoryGirl(Skill).create()

      // with optimistic lock
      Skill.deleteByIdAndVersion(skill.id, skill.lockVersion)
      intercept[OptimisticLockException] {
        Skill.deleteByIdAndVersion(skill.id, skill.lockVersion)
      }
      // without lock
      Skill.deleteById(skill.id)
    }

    it("should update with lock timestamp") { implicit session =>
      val n = Name.column
      val member = FactoryGirl(Member)
        .withValues("countryId" -> FactoryGirl(Country, "countryyy").create().id)
        .create("companyId" -> FactoryGirl(Company).create().id, "createdAt" -> DateTime.now)
      val name = FactoryGirl(Name).create("memberId" -> member.id)

      // with optimistic lock
      Name.updateByIdAndTimestamp(name.memberId, name.updatedAt).withNamedValues(n.first -> "Kaz")
      intercept[OptimisticLockException] {
        Name.updateByIdAndTimestamp(name.memberId, name.updatedAt).withNamedValues(n.first -> "Kaz")
      }
      // without lock
      Name.updateById(name.memberId).withNamedValues(n.first -> "Kaz")
    }

    it("should delete with lock timestamp") { implicit session =>
      val member = FactoryGirl(Member)
        .withValues("countryId" -> FactoryGirl(Country, "countryyy").create().id)
        .create("companyId" -> FactoryGirl(Company).create().id, "createdAt" -> DateTime.now)
      val name = FactoryGirl(Name).create("memberId" -> member.id)

      // with optimistic lock
      Name.deleteByIdAndTimestamp(name.memberId, name.updatedAt)
      intercept[OptimisticLockException] {
        Name.deleteByIdAndTimestamp(name.memberId, name.updatedAt)
      }
      // without lock
      Name.deleteById(name.memberId)
    }
  }

  describe("FactoryGirl") {

    it("should work") { implicit session =>
      val company1 = FactoryGirl(Company).create()
      company1.name should equal("FactoryGirl")

      val company2 = FactoryGirl(Company).create("name" -> "FactoryPal")
      company2.name should equal("FactoryPal")

      val country = FactoryGirl(Country, "countryyy").create()

      val memberFactory = FactoryGirl(Member).withValues("countryId" -> country.id)
      val member = memberFactory.create("companyId" -> company1.id, "createdAt" -> DateTime.now)
      val name = FactoryGirl(Name).create("memberId" -> member.id)

      name.first should equal("Kazuhiro")
      name.last should equal("Sera")
      name.member.get.id should equal(member.id)

      val skill = FactoryGirl(Skill).create()
      skill.name should equal("Scala Programming")
    }
  }

}
