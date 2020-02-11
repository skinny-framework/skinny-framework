package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.micro.Context
import skinny.orm.SkinnyCRUDMapper
import skinny.routing.Routes

class AsyncSkinnyServletSpec extends ScalatraFlatSpec {

  behavior of "AsyncSkinnyApiServlet"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add("AsyncSkinnyApiServlet", "jdbc:h2:mem:AsyncSkinnyApiServlet", "", "")
  NamedDB("AsyncSkinnyApiServlet").localTx { implicit s =>
    sql"create table company (id serial primary key, name varchar(64), url varchar(128));".execute.apply()
  }

  case class Company(id: Long, name: String, url: String)
  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName = "AsyncSkinnyApiServlet"
    override def defaultAlias       = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = new Company(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  class CompaniesController extends AsyncSkinnyApiServlet {
    def create(implicit ctx: Context) = {
      val count = Company.createWithAttributes(
        "name" -> params.getAs[String]("name"),
        "url"  -> params.getAs[String]("url")
      )
      if (count == 1) status = 201 else status = 400
    }
    def list(implicit ctx: Context) = toPrettyJSONString(Company.findAll())
  }
  val controller = new CompaniesController with Routes {
    val creationUrl = post("/companies")(implicit ctx => create).as("list")
    val listUrl     = get("/companies.json")(implicit ctx => list).as("list")
  }

  addServlet(controller, "/*")

  it should "have creation API" in {
    post("/companies", "name" -> "CompanyName", "url" -> "http://www.example.com/") {
      header("X-Content-Type-Options") should equal("nosniff")
      status should equal(201)
    }
  }

  it should "have list API" in {
    get("/companies.json") {
      status should equal(200)
    }
    get("/companies.xml") {
      status should equal(404)
    }
  }

}
