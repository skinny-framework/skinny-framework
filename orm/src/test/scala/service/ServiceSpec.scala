package service

import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback

import org.scalatest._

class ServiceSpec extends fixture.FunSpec with Matchers with Connection with CreateTables with AutoRollback {

  override def db(): DB = NamedDB("service").toDB()

  override def fixture(implicit session: DBSession): Unit = {
    val serviceNo = Service.createWithAttributes("name" -> "Cool Web Service")
    Application.createWithAttributes("name" -> "Smartphone site", "serviceNo"   -> serviceNo)
    Application.createWithAttributes("name" -> "PC site", "serviceNo"           -> serviceNo)
    Application.createWithAttributes("name" -> "Featurephone site", "serviceNo" -> serviceNo)
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

      val beforeService = Service.joins(Service.applications).findById(app.serviceNo).get

      Application.deleteById(beforeService.applications.head.id)

      val afterService = Service.joins(Service.applications).findById(app.serviceNo).get
      afterService.applications.size should equal(
        beforeService.applications.size - 1
      )
    }
  }
}
