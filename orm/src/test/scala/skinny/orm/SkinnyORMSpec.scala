package skinny.orm

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import skinny.test.FactoryGirl
import skinny.orm.exception.OptimisticLockException
import skinny.{ ParamType, StrongParameters }

class SkinnyORMSpec extends fixture.FunSpec with ShouldMatchers
    with Connection
    with CreateTables
    with Formatter
    with AutoRollback {

  override def fixture(implicit session: DBSession) {

    // #createWithNamedValues
    val countryId1 = Country.createWithAttributes('name -> "Japan")
    val countryId2 = Country.createWithAttributes('name -> "China")

    val groupId1 = GroupMapper.createWithAttributes('name -> "Scala Users Group")
    val groupId2 = GroupMapper.createWithAttributes('name -> "Java Group")

    val companyId = Company.createWithAttributes('name -> "Typesafe", 'countryId -> countryId1)

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

    it("returns nested belongsTo relations") { implicit session =>
      val m = Member.defaultAlias

      val member = Member.findAllByPaging(sqls.isNotNull(m.companyId), 1, 0).head
      member.company.get.country.isDefined should be(false)

      val memberWithCountry = Member.includes(Member.companyOpt).findAllByPaging(sqls.isNotNull(m.companyId), 1, 0).head
      memberWithCountry.company.get.country.isDefined should be(true)
    }

    it("returns nested hasMany relations") { implicit session =>
      val company = Company.joins(Company.members).findAll().find(_.members.size > 0).head
      company.members.head.name.isDefined should be(false)

      val includedCompany = Company.includes(Company.members).findAll().find(_.members.size > 0).head
      includedCompany.members.head.name.isDefined should be(true)
    }

    it("should have #findAll()") { implicit session =>
      Member.findAll().size should be > (0)
      val m = Member.defaultAlias
      Member.findAll(ordering = sqls"${m.id}, ${m.createdAt} desc").size should be > (0)
    }

    it("should have #countAll()") { implicit session =>
      Member.countAll() should be > (0L)
    }

    it("should have #findAllBy(SQLSyntax, Int, Int)") { implicit session =>
      val countryId = Country.findAll().map(_.id).head
      Member.withAlias { m =>
        Member.findAllBy(sqls.eq(m.countryId, countryId)).size should be > (0)
        Member.findAllByPaging(sqls.eq(m.countryId, countryId), 1, 0).size should equal(1)

        val ordering = sqls"${m.id}, ${m.createdAt} desc"
        Member.findAllBy(sqls.eq(m.countryId, countryId), ordering).size should be > (0)
        Member.findAllByPaging(sqls.eq(m.countryId, countryId), 1, 0, ordering).size should equal(1)
      }
    }

    it("should have #countBy(SQLSyntax)") { implicit session =>
      val countryId = Country.limit(1).offset(0).apply().map(_.id).head
      Member.withAlias(s =>
        Member.countBy(sqls.eq(s.countryId, countryId))
      ) should be > (0L)
    }

    it("should have #updateById(Long)") { implicit session =>
      val countryId = Country.limit(1).offset(0).apply().map(_.id).head
      val memberId = Member.createWithAttributes(
        'countryId -> countryId, 'createdAt -> DateTime.now
      )
      val mentorId = Member.limit(1).offset(0).apply().head.id
      Member.updateById(memberId).withAttributes('mentorId -> mentorId)
      val updated = Member.findById(memberId)
      updated.get.mentorId should equal(Some(mentorId))
    }

    it("should have #deleteById(Long)") { implicit session =>
      val countryId = Country.limit(1).offset(0).apply().map(_.id).head
      val memberId = Member.createWithAttributes(
        'countryId -> countryId, 'createdAt -> DateTime.now
      )
      Member.deleteById(memberId)
    }

    it("should have #createWithPermittedAttributes") { implicit session =>
      val minutes = java.util.TimeZone.getDefault.getRawOffset / 1000 / 60
      val prefix = if (minutes >= 0) "+" else "-"
      val timeZone = prefix + "%02d:%02d".format((math.abs(minutes) / 60), (math.abs(minutes) % 60))
      val minus2hours = minutes - 120
      val minus2hoursPrefix = if (minus2hours >= 0) "+" else "-"
      val minus2hoursTimeZone = minus2hoursPrefix + "%02d:%02d".format((math.abs(minus2hours) / 60), (math.abs(minus2hours) % 60))
      Seq(
        s"2013-01-02T03:04:05${timeZone}",
        s"2013-01-02T01:04:05${minus2hoursTimeZone}",
        s"2013/01/02T03:04:05${timeZone}",
        s"2013-01-02 03:04:05${timeZone}",
        s"2013-01-02T03:04:05",
        s"2013-01-02 03:04:05",
        s"2013/1/2 3:4:5",
        s"2013-1-2 03:4:05",
        s"2013-01-02 03-04-05"
      ) foreach { createdAt =>
          val params = StrongParameters(Map("name" -> "Java Programming", "createdAt" -> createdAt))
          val id = Skill.createWithPermittedAttributes(params.permit("name" -> ParamType.String, "createdAt" -> ParamType.DateTime))
          val created = Skill.findById(id)
          created.get.createdAt should equal(new DateTime(2013, 1, 2, 3, 4, 5))
        }
    }

    it("should thorw exception for invalid datetime format") { implicit session =>
      Seq(
        "2013-a-b 03-04-05",
        "2013-01-02 0c:04:05"
      ) foreach { createdAt =>
          intercept[Exception] {
            Skill.createWithPermittedAttributes(
              StrongParameters(Map("name" -> "Java Programming", "createdAt" -> createdAt)).permit("name" -> ParamType.String, "createdAt" -> ParamType.DateTime))
          }
        }
    }

    it("should have querying APIs") { implicit session =>
      val allMembers = Member.findAll()

      val japan = Country.where('name -> "Japan").limit(1000).offset(0).apply().head
      val expected = allMembers.filter(_.countryId == japan.id)

      val m = Member.defaultAlias
      val actual = Member.where(sqls.eq(m.countryId, japan.id)).limit(1000).offset(0).apply()
      actual should equal(expected)
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
        val membersByIds = Member.where('id -> members.map(_.id)).apply()
        membersByIds.size should equal(members.size)
        Member.where('id -> members.map(_.id)).count.apply() should equal(members.size)

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

        {
          val membersWithSkills = Member.joins(Member.skills).where('id -> Member.findAll().map(_.id)).apply()
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
        .withValues('countryId -> FactoryGirl(Country, 'countryyy).create().id)
        .create('companyId -> FactoryGirl(Company).create().id, 'createdAt -> DateTime.now)
      val name = FactoryGirl(Name).create('memberId -> member.id)

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
        .withValues('countryId -> FactoryGirl(Country, 'countryyy).create().id)
        .create('companyId -> FactoryGirl(Company).create().id, 'createdAt -> DateTime.now)
      val name = FactoryGirl(Name).create('memberId -> member.id)

      // with optimistic lock
      Name.deleteByIdAndOptionalTimestamp(name.memberId, name.updatedAt)
      intercept[OptimisticLockException] {
        Name.deleteByIdAndOptionalTimestamp(name.memberId, name.updatedAt)
      }
      // without lock
      Name.deleteById(name.memberId)
    }
  }

  describe("dynamic table name") {
    it("should accept table name") { implicit session =>
      val createdId = Member.withColumns { m =>
        Member.withTableName("members2").createWithNamedValues(
          m.countryId -> Country.limit(1).apply().head.id,
          m.companyId -> Company.limit(1).apply().head.id,
          m.createdAt -> DateTime.now
        )
      }
      Member.withTableName("members2").findAll().exists(_.id == createdId) should be(true)
      Member.findAll().exists(_.id == createdId) should be(false)
    }
  }

  describe("FactoryGirl") {
    it("should work") { implicit session =>

      val company1 = FactoryGirl(Company).create()
      company1.name should equal("FactoryGirl")

      val company2 = FactoryGirl(Company).create('name -> "FactoryPal")
      company2.name should equal("FactoryPal")

      val country = FactoryGirl(Country, 'countryyy).create()

      val memberFactory = FactoryGirl(Member).withValues('countryId -> country.id)
      val member = memberFactory.create('companyId -> company1.id, 'createdAt -> DateTime.now)
      val name = FactoryGirl(Name).create('memberId -> member.id)

      name.first should equal("Kazuhiro")
      name.last should equal("Sera")
      name.member.get.id should equal(member.id)

      val skill = FactoryGirl(Skill).create()
      skill.name should equal("Scala Programming")

    }
  }

}
