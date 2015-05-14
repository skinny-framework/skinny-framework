package skinny.orm.feature

import org.scalatest._
import scalikejdbc._
import skinny.orm._
import skinny.orm.exception.AssociationSettingsException

class AssociationsFeatureSpec extends FlatSpec with Matchers {
  behavior of "AssociationsFeature"

  Class.forName("org.h2.Driver")
  ConnectionPool.add('AssociationsFeatureSpec, "jdbc:h2:mem:AssociationsFeatureSpec;MODE=PostgreSQL", "sa", "sa")

  NamedDB('AssociationsFeatureSpec).autoCommit { implicit s =>
    sql"create table company(id bigserial, name varchar(100) not null)".execute.apply()
    sql"create table person(id bigserial, name varchar(100) not null, company_id bigint references company(id))".execute.apply()
    sql"create table department(id bigserial, name varchar(100) not null, company_id bigint references company(id), primary key (id, company_id))".execute.apply()
    sql"create table address(id bigserial, address varchar(100) not null, company_id bigint references company(id))".execute.apply()
    sql"create table company_department(company_id bigint references company(id), department_id bigint references department(id), primary key (company_id, department_id))".execute.apply()
  }

  it should "have #defaultIncludesMerge" in {
    intercept[AssociationSettingsException] {
      AssociationsFeature.defaultIncludesMerge(Nil, Nil)
    }
  }

  case class Person(id: Long, name: String, companyId: Option[Long], company: Option[Company] = None)
  case class Company(id: Long, name: String, address: Option[Address] = None, departments: Seq[Department] = Nil)
  case class Address(id: Long, address: String, companyId: Option[Long])
  case class Department(id: Long, companyId: Option[Long], name: String, company: Option[Company] = None)
  case class CompanyDepartment(companyId: Long, departmentId: Long)

  object Person extends SkinnyMapper[Person] {
    override def connectionPoolName = 'AssociationsFeatureSpec
    override def defaultAlias = createAlias("p")
    override def extract(rs: WrappedResultSet, n: ResultName[Person]) = autoConstruct(rs, n, "company")
  }
  object Company extends SkinnyMapper[Company] {
    override def connectionPoolName = 'AssociationsFeatureSpec
    override def defaultAlias = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = autoConstruct(rs, n, "address", "departments")
  }
  object Address extends SkinnyMapper[Address] {
    override def connectionPoolName = 'AssociationsFeatureSpec
    override def defaultAlias = createAlias("a")
    override def extract(rs: WrappedResultSet, n: ResultName[Address]) = autoConstruct(rs, n)
  }
  object Department extends SkinnyNoIdMapper[Department] {
    override def connectionPoolName = 'AssociationsFeatureSpec
    override def defaultAlias = createAlias("d")
    override def extract(rs: WrappedResultSet, n: ResultName[Department]) = autoConstruct(rs, n, "company")
  }
  object CompanyDepartment extends SkinnyJoinTable[CompanyDepartment] {
    override def connectionPoolName = 'AssociationsFeatureSpec
    override def defaultAlias = createAlias("cd")
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
    Person.join(Company -> Company.defaultAlias, (p, c) => sqls"")
    Person.join[Company](Company -> Company.defaultAlias, Person -> Person.defaultAlias, (c, p) => sqls"")
  }

  it should "have #innerJoin" in {
    Person.innerJoin(Company -> Company.defaultAlias, (p, c) => sqls"")
    Person.innerJoin[Company](Company -> Company.defaultAlias, Person -> Person.defaultAlias, (c, p) => sqls"")
  }

  it should "have #hasOne" in {
    Company.hasOne[Address](Address, (c, a) => c.copy(address = a))
  }

  it should "have #hasOneWithAlias" in {
    Company.hasOneWithAlias[Address](Address -> Address.defaultAlias, (c, a) => c.copy(address = a))
  }

  it should "have #hasOneWithAliasAndJoinCondition" in {
    Company.hasOneWithAliasAndJoinCondition[Address](Address -> Address.defaultAlias, sqls.eq(Company.column.id, Address.column.companyId), (c, a) => c.copy(address = a))
  }

  it should "have #hasOneWithAliasAndFk" in {
    Company.hasOneWithAliasAndFk[Address](Address -> Address.defaultAlias, "company_id", (c, a) => c.copy(address = a))
  }

  it should "have #hasOneWithAliasAndFkAndJoinCondition" in {
    Company.hasOneWithAliasAndFkAndJoinCondition[Address](Address -> Address.defaultAlias, "company_id", sqls.eq(Company.column.id, Address.column.companyId), (c, a) => c.copy(address = a))
  }

  it should "have #belongsTo" in {
    Person.belongsTo[Company](Company, (p, c) => p.copy(company = c))
  }

  it should "have #belongsToWithAlias" in {
    Person.belongsToWithAlias[Company](Company -> Company.defaultAlias, (p, c) => p.copy(company = c))
  }

  it should "have #belongsToWithFk" in {
    Person.belongsToWithFk[Company](Company, "id", (p, c) => p.copy(company = c))
  }

  it should "have #belongsToWithAliasAndFk" in {
    Person.belongsToWithAliasAndFk[Company](Company -> Company.defaultAlias, "id", (p, c) => p.copy(company = c))
  }

  it should "have #belongsToWithAliasAndFkAndJoinCondition" in {
    Person.belongsToWithAliasAndFkAndJoinCondition[Company](Company -> Company.defaultAlias, "id", sqls"", (p, c) => p.copy(company = c))
  }

  it should "have #hasMany" in {
    Company.hasMany[Department](Department -> Department.defaultAlias, (c, d) => sqls.eq(c.id, d.companyId), (c, d) => c.copy(departments = d))
  }

  it should "have #hasManyThrough" in {
    Company.hasManyThrough[Department](CompanyDepartment, Department, (c, d) => c.copy(departments = d))
  }

  it should "have #hasManyThroughWithFk" in {
    Company.hasManyThroughWithFk[Department](CompanyDepartment, Department, "company_id", "department_id", (c, d) => c.copy(departments = d))
  }

  it should "have #extract" in {
    Person.extract(sql"")
  }

}
