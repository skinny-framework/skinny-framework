package test003

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.exception.IllegalAssociationException
import skinny.orm.{ SkinnyCRUDMapper, SkinnyNoIdCRUDMapper }

class Spec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB(Symbol("test003")).toDB()

  // entities
  case class Person(id: Int, name: String)
  case class Company(id: Int, name: String)
  case class Employee(
      companyId: Int,
      personId: Int,
      role: Option[String],
      company: Option[Company] = None,
      person: Option[Person] = None
  )

  // mappers
  object Person extends SkinnyCRUDMapper[Person] {
    override val connectionPoolName                                    = Symbol("test003")
    override lazy val defaultAlias                                     = createAlias("p")
    override def extract(rs: WrappedResultSet, rn: ResultName[Person]) = autoConstruct(rs, rn)
  }

  object Company extends SkinnyCRUDMapper[Company] {
    override val connectionPoolName                                     = Symbol("test003")
    override lazy val defaultAlias                                      = createAlias("c")
    override def extract(rs: WrappedResultSet, rn: ResultName[Company]) = autoConstruct(rs, rn)
  }

  object Employee extends SkinnyNoIdCRUDMapper[Employee] {
    override val connectionPoolName                                      = Symbol("test003")
    override lazy val defaultAlias                                       = createAlias("e")
    override def extract(rs: WrappedResultSet, rn: ResultName[Employee]) = autoConstruct(rs, rn, "company", "person")

    lazy val personRef  = belongsTo[Person](Person, (e, p) => e.copy(person = p))
    lazy val companyRef = belongsTo[Company](Company, (e, c) => e.copy(company = c))

    lazy val hasOneRef = hasOne[Company](Company, (e, c) => e.copy(company = c))

    lazy val withAssociations = joins(companyRef, personRef)
  }

  override def fixture(implicit session: DBSession): Unit = {
    val p1 = Person.createWithAttributes(Symbol("name")        -> "Alice")
    val p2 = Person.createWithAttributes(Symbol("name")        -> "Bob")
    val p3 = Person.createWithAttributes(Symbol("name")        -> "Chris")
    val c1 = Company.createWithAttributes(Symbol("name")       -> "Google")
    val e1 = Employee.createWithAttributes(Symbol("companyId") -> c1, Symbol("personId") -> p1)
    val e2 =
      Employee.createWithAttributes(Symbol("companyId") -> c1, Symbol("personId") -> p2, Symbol("role") -> "Engineer")
  }

  describe("Entities with compound primary keys") {
    it("should have finder APIs") { implicit session =>
      Employee.findAll().size should equal(2)
      Employee.withAssociations.findAll().size should equal(2)
    }

    it("should have querying APIs") { implicit session =>
      val e   = Employee.defaultAlias
      val es1 = Employee.where(sqls.eq(e.companyId, Company.limit(1).apply().head.id)).apply()
      es1.size should equal(2)
    }

    it("should detect invalid associations") { implicit session =>
      intercept[IllegalAssociationException] {
        Employee.joins(Employee.hasOneRef).count()
      }
    }

  }

}
