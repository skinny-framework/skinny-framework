package skinny.orm.feature

import org.scalatest._
import scalikejdbc._
import skinny.orm._
import skinny.orm.exception.AssociationSettingsException

class AssociationsFeatureSpec extends FlatSpec with Matchers {
  behavior of "AssociationsFeature"

  Class.forName("org.h2.Driver")
  ConnectionPool.add("AssociationsFeatureSpec", "jdbc:h2:mem:AssociationsFeatureSpec;MODE=PostgreSQL", "sa", "sa")

  NamedDB("AssociationsFeatureSpec").autoCommit { implicit s =>
    sql"create table company(id bigserial, name varchar(100) not null)".execute.apply()
    sql"create table person(id bigserial, name varchar(100) not null, company_id bigint references company(id))".execute
      .apply()
  }

  it should "have #defaultIncludesMerge" in {
    intercept[AssociationSettingsException] {
      AssociationsFeature.defaultIncludesMerge(Nil, Nil)
    }
  }

  case class Person(id: Long, name: String, companyId: Option[Long], company: Option[Company] = None)
  case class Company(id: Long, name: String)

  object Person extends SkinnyMapper[Person] {
    override def connectionPoolName                                   = "AssociationsFeatureSpec"
    override def defaultAlias                                         = createAlias("p")
    override def extract(rs: WrappedResultSet, n: ResultName[Person]) = autoConstruct(rs, n, "company")
  }
  object Company extends SkinnyMapper[Company] {
    override def connectionPoolName                                    = "AssociationsFeatureSpec"
    override def defaultAlias                                          = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = autoConstruct(rs, n)
  }

  it should "have #joinWithDefaults" in {
    Person.joinWithDefaults(Company, sqls"foo")
    Person.joinWithDefaults(Company, (p, c) => sqls.eq(p.companyId, c.id))
    Person.joinWithDefaults[Company](Company, Person, (c, p) => sqls.eq(p.companyId, c.id))
  }

  it should "have #innerJoinWithDefaults" in {
    Person.innerJoinWithDefaults(Company, (p, c) => sqls.eq(p.companyId, c.id))
    Person.innerJoinWithDefaults[Company](Company, Person, (c, p) => sqls.eq(p.companyId, c.id))
  }

  it should "have #leftJoinWithDefaults" in {
    Person.leftJoinWithDefaults(Company, sqls"foo")
    Person.leftJoinWithDefaults(Company, (p, c) => sqls.eq(p.companyId, c.id))
    Person.leftJoinWithDefaults(Person, Company, (p, c) => sqls.eq(p.companyId, c.id))
  }

  it should "have #join" in {
    Person.join(Company          -> Company.defaultAlias, (p, c) => sqls"")
    Person.join[Company](Company -> Company.defaultAlias, Person -> Person.defaultAlias, (c, p) => sqls"")
  }

  it should "have #innerJoin" in {
    Person.innerJoin(Company          -> Company.defaultAlias, (p, c) => sqls"")
    Person.innerJoin[Company](Company -> Company.defaultAlias, Person -> Person.defaultAlias, (c, p) => sqls"")
  }

  it should "have #hasOneWithAlias" in {
    Company.hasOneWithAlias[Person](Person -> Person.defaultAlias, (c, p) => c)
  }

  it should "have #hasOneWithAliasAndJoinCondition" in {
    Person.hasOneWithAliasAndJoinCondition[Company](Company -> Company.defaultAlias, sqls"", (p, c) => p)
  }

  it should "have #hasOneWithAliasAndFk" in {
    Person.hasOneWithAliasAndFk[Company](Company -> Company.defaultAlias, "id", (p, c) => p)
  }

  it should "have #hasOneWithAliasAndFkAndJoinCondition" in {
    Person.hasOneWithAliasAndFkAndJoinCondition[Company](Company -> Company.defaultAlias, "id", sqls"", (p, c) => p)
  }

  it should "have #extract" in {
    Person.extract(sql"")
  }

}
