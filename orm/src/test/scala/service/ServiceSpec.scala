package service

import scalikejdbc._, SQLInterpolation._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers

class ServiceSpec extends fixture.FunSpec with ShouldMatchers
    with Connection
    with CreateTables
    with AutoRollback {

  override def db(): DB = NamedDB('service).toDB()

  override def fixture(implicit session: DBSession) {
    val serviceId = Service.createWithAttributes('name -> "Cool Web Service")
    Application.createWithAttributes('name -> "Smartphone site", 'serviceId -> serviceId)
    Application.createWithAttributes('name -> "PC site", 'serviceId -> serviceId)
    Application.createWithAttributes('name -> "Featurephone site", 'serviceId -> serviceId)
  }

  describe("hasMany without byDefault") {
    it("should work as expected") { implicit session =>
      val service = Service.joins(Service.applications).findAll().head
      service.applications.size should equal(3)
    }
  }

  describe("belongsTo without byDefault") {
    it("should work as expected") { implicit session =>
      val app = Application.joins(Application.service).findAll().head
      app.service.isDefined should equal(true)
    }
  }
}
