package skinny.orm

import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature._
import org.joda.time.DateTime
import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import skinny.test.FactoryGirl
import skinny.orm.exception.OptimisticLockException
import org.slf4j.LoggerFactory

class SkinnyORMSpec extends fixture.FunSpec with ShouldMatchers
    with Connection
    with Formatter
    with CreateTables
    with AutoRollback {

  override def fixture(implicit session: DBSession) {

    // #createWithNamedValues
    val countryId1 = Country.createWithAttributes('name -> "Japan")
    val countryId2 = Country.createWithAttributes('name -> "China")

    val groupId1 = GroupMapper.createWithAttributes('name -> "Scala Users Group")
    val groupId2 = GroupMapper.createWithAttributes('name -> "Java Group")

    val companyId = Company.createWithAttributes('name -> "Typesafe")

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

      Name.createWithAttributes('memberId -> alice, 'first -> "Alice", 'last -> "Cooper")
      Name.createWithAttributes('memberId -> bob, 'first -> "Bob", 'last -> "Marley")
      Name.createWithAttributes('memberId -> chris, 'first -> "Chris", 'last -> "Birchall")

      GroupMember.createWithAttributes('memberId -> alice, 'groupId -> groupId1)
      GroupMember.createWithAttributes('memberId -> bob, 'groupId -> groupId1)
      GroupMember.createWithAttributes('memberId -> bob, 'groupId -> groupId2)

      val skillId = Skill.createWithAttributes('name -> "Programming")
      Skill.updateById(skillId).withAttributes('name -> "Web development")
      MemberSkill.createWithAttributes('memberId -> alice, 'skillId -> skillId)
    }

  }

  describe("MutableSkinnyRecord") {
    it("should act like ActiveRecord") { implicit session =>
      val newCompany = Company(name = "Sun")
      newCompany.isNewRecord should be(true)
      newCompany.isPersisted should be(false)

      val companyId = newCompany.create()

      val company = Company.findById(companyId).get
      company.isNewRecord should be(false)
      company.isPersisted should be(true)

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
      val countryId = Country.createWithAttributes('name -> "Brazil")
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
        Member.findAllBy(sqls.eq(m.countryId, countryId)).size should be > (0)
        Member.findAllByPaging(sqls.eq(m.countryId, countryId), 1, 0).size should equal(1)
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
      val memberId = Member.createWithAttributes(
        'countryId -> countryId, 'createdAt -> DateTime.now
      )
      val mentorId = Member.findAll().head.id
      Member.updateById(memberId).withAttributes('mentorId -> mentorId)
      val updated = Member.findById(memberId)
      updated.get.mentorId should equal(Some(mentorId))
    }

    it("should have #deleteById(Long)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      val memberId = Member.createWithAttributes(
        'countryId -> countryId, 'createdAt -> DateTime.now
      )
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
      val skill = FactoryGirl(Skill).create()

      // with optimistic lock
      Skill.updateByIdAndVersion(skill.id, skill.lockVersion).withAttributes('name -> "Java Programming")
      intercept[OptimisticLockException] {
        Skill.updateByIdAndVersion(skill.id, skill.lockVersion).withAttributes('name -> "Ruby Programming")
      }
      // without lock
      Skill.updateById(skill.id).withAttributes('name -> "Ruby Programming")
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
      val member = FactoryGirl(Member)
        .withValues("countryId" -> FactoryGirl(Country, "countryyy").create().id)
        .create("companyId" -> FactoryGirl(Company).create().id, "createdAt" -> DateTime.now)
      val name = FactoryGirl(Name).create("memberId" -> member.id)

      // with optimistic lock
      Name.updateByIdAndTimestamp(name.memberId, name.updatedAt).withAttributes('first -> "Kaz")
      intercept[OptimisticLockException] {
        Name.updateByIdAndTimestamp(name.memberId, name.updatedAt).withAttributes('first -> "Kaz")
      }
      // without lock
      Name.updateById(name.memberId).withAttributes('first -> "Kaz")
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
