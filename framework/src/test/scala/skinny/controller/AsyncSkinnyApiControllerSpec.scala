package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.micro.Context
import skinny.orm.SkinnyCRUDMapper
import skinny.routing.Routes

class AsyncSkinnyApiControllerSpec extends ScalatraFlatSpec {

  behavior of "AsyncSkinnyApiController"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add("AsyncSkinnyApiController", "jdbc:h2:mem:AsyncSkinnyApiController", "", "")
  NamedDB("AsyncSkinnyApiController").localTx { implicit s =>
    sql"create table company (id serial primary key, name varchar(64), url varchar(128));".execute.apply()
  }

  case class Company(id: Long, name: String, url: String)
  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName = "AsyncSkinnyApiController"
    override def defaultAlias       = createAlias("c")
    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = new Company(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  class CompaniesController extends AsyncSkinnyApiController {
    beforeAction() { implicit ctx =>
      set("foo" -> "bar")
    }
    afterAction() { implicit ctx =>
      response.headers += "foo" -> "bar"
    }

    def create(implicit ctx: Context) = {
      val count = Company.createWithAttributes(
        "name" -> params.getAs[String]("name"),
        "url"  -> params.getAs[String]("url")
      )
      if (count == 1) status = 201 else status = 400
    }
    def list(implicit ctx: Context) = warnElapsedTimeWithRequest(1) {
      Thread.sleep(10)
      toPrettyJSONString(Company.findAll())
    }

    def filter(implicit ctx: Context) = {
      requestScope.getOrElse("foo", "")
    }
  }
  val controller = new CompaniesController with Routes {
    val creationUrl     = post("/companies")(implicit ctx => create).as("list")
    val listUrl         = get("/companies.json")(implicit ctx => list).as("list")
    val beforeFilterUrl = get("/filter")(implicit ctx => filter).as("filter")
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

  it should "work with async filters" in {
    get("/filter") {
      status should equal(200)
      body should equal("bar")
      header("foo") should equal("bar")
    }
  }

}
