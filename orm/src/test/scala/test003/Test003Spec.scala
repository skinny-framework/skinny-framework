package test003

import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.orm._

class Test003Spec extends fixture.FunSpec with Matchers
    with Connection
    with CreateTables
    with AutoRollback {

  case class Person(id: Int, name: String)
  case class Company(id: Int, name: String)
  case class Employee(
    companyId: Int, personId: Int, role: Option[String],
    company: Option[Company] = None,
    person: Option[Person] = None)

  object Person extends SkinnyCRUDMapper[Person] {
    override def connectionPoolName = 'test003
    override def defaultAlias = createAlias("p")
    override def extract(rs: WrappedResultSet, rn: ResultName[Person]) = autoConstruct(rs, rn)
  }

  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName = 'test003
    override def defaultAlias = createAlias("c")
    override def extract(rs: WrappedResultSet, rn: ResultName[Company]) = autoConstruct(rs, rn)
  }

  object Employee extends SkinnyNoIdCRUDMapper[Employee] {
    override def connectionPoolName = 'test003
    override def defaultAlias = createAlias("e")
    override def extract(rs: WrappedResultSet, rn: ResultName[Employee]) = (rs, rn, "company", "person")

    lazy val personRef = belongsTo[Person](Person, (e, p) => e.copy(person = p))
    lazy val companyRef = belongsTo[Company](Company, (e, c) => e.copy(company = c))

    def withAssociations = joins(Employee.companyRef, personRef)
  }

  override def db(): DB = NamedDB('test003).toDB()

  override def fixture(implicit session: DBSession) {
    val p1 = Person.createWithAttributes('name -> "Alice")
    val p2 = Person.createWithAttributes('name -> "Bob")
    val p3 = Person.createWithAttributes('name -> "Chris")
    val c1 = Company.createWithAttributes('name -> "Google")
    val e1 = Employee.createWithAttributes('companyId -> c1, 'personId -> p1)
    val e2 = Employee.createWithAttributes('companyId -> c1, 'personId -> p2, 'role -> "Engineer")
  }

  describe("Entities with compound primary keys") {
    it("should work as expected") { implicit session =>

      Employee.findAll().size should equal(2)

      Employee.withAssociations.findAll().size should equal(2)

      val e = Employee.defaultAlias
      val es1 = Employee.where(sqls.eq(e.companyId, Company.limit(1).apply().head.id)).apply()
      es1.size should equal(2)

      val es2 = Employee.withAssociations.where(sqls.eq(e.companyId, Company.limit(1).apply().head.id)).apply()
      es2.size should equal(2)
    }
  }

}
