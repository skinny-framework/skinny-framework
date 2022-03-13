package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.orm.SkinnyCRUDMapper
import skinny.Routes

class SkinnyApiControllerSpec extends ScalatraFlatSpec {

  behavior of "SkinnyApiController"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add("SkinnyApiController", "jdbc:h2:mem:SkinnyApiController", "", "")
  NamedDB("SkinnyApiController").localTx { implicit s =>
    sql"create table company (id serial primary key, name varchar(64), url varchar(128));".execute.apply()
  }

  case class Company(id: Long, name: String, url: String)
  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName = "SkinnyApiController"
    override def defaultAlias       = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = new Company(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  class CompaniesController extends SkinnyApiController {
    def create = {
      val count = Company.createWithAttributes(
        "name" -> params.getAs[String]("name"),
        "url"  -> params.getAs[String]("url")
      )
      if (count == 1) status = 201 else status = 400
    }
    def list = warnElapsedTimeWithRequest(1) {
      Thread.sleep(10)
      toPrettyJSONString(Company.findAll())
    }
  }
  val controller = new CompaniesController with Routes {
    val creationUrl = post("/companies")(create).as("list")
    val listUrl     = get("/companies.json")(list).as("list")
  }

  addFilter(controller, "/*")

  it should "have creation API" in {
    post("/companies", "name" -> "CompanyName", "url" -> "http://www.example.com/") {
      header("X-Content-Type-Options") should equal("nosniff")
      status should equal(201)
    }
  }

  it should "have list API" in {
    get("/companies.json?foo=bar&baz=123&password=foo") {
      status should equal(200)
    }
    get("/companies.xml") {
      status should equal(404)
    }
  }

}
