package skinny.orm.feature

import org.scalatest._
import scalikejdbc._
import skinny._
import skinny.orm._

class CRUDFeatureSpec extends FlatSpec with Matchers {
  behavior of "CRUDFeature"

  Class.forName("org.h2.Driver")
  ConnectionPool.add("CRUDFeatureSpec", "jdbc:h2:mem:CRUDFeatureSpec;MODE=PostgreSQL", "sa", "sa")

  NamedDB("CRUDFeatureSpec").autoCommit { implicit s =>
    sql"create table company(id bigserial, name varchar(100) not null)".execute.apply()
    sql"create table person(id bigserial, name varchar(100) not null, company_id bigint references company(id))".execute
      .apply()
  }

  case class Person(id: Long, name: String, companyId: Option[Long], company: Option[Company] = None)
  case class Company(id: Long, name: String)

  object Person extends SkinnyCRUDMapper[Person] {
    override def connectionPoolName                                   = "CRUDFeatureSpec"
    override def defaultAlias                                         = createAlias("p")
    override def extract(rs: WrappedResultSet, n: ResultName[Person]) = autoConstruct(rs, n, "company")
  }
  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName                                    = "CRUDFeatureSpec"
    override def defaultAlias                                          = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = autoConstruct(rs, n)
  }

  // SkinnyModel APIs

  it should "have #createNewModel" in {
    Person.createNewModel(StrongParameters(Map("name" -> "Alice")).permit("name" -> ParamType.String))
  }

  it should "have #findAllModels" in {
    Person.findAllModels()
  }

  it should "have #countAllModels" in {
    Person.countAllModels()
  }

  it should "have #findModels" in {
    Person.findModels(1, 1)
  }
  it should "have #findModel" in {
    val id = Person.createWithAttributes("name" -> "Alice")
    Person.findModel(id).isDefined should equal(true)
  }

  it should "have #updateModelById" in {
    val id = Person.createWithAttributes("name" -> "Alice")
    Person.updateModelById(id, StrongParameters(Map("name" -> "Bob")).permit("name" -> ParamType.String))
  }

  it should "have #deleteModelById" in {
    val id = Person.createWithAttributes("name" -> "Alice")
    Person.deleteModelById(id)
  }

}
