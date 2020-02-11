package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.ParamType
import skinny.orm.SkinnyCRUDMapper
import skinny.validator._

class SkinnyResourceServletSpec extends ScalatraFlatSpec {

  behavior of "SkinnyResourceServlet"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add("SkinnyResourceServlet", "jdbc:h2:mem:SkinnyResourceServlet", "", "")
  NamedDB("SkinnyResourceServlet").localTx { implicit s =>
    sql"create table api (id serial primary key, name varchar(64) not null, url varchar(128) not null);".execute
      .apply()
  }

  case class Api(id: Long, name: String, url: String)
  object Api extends SkinnyCRUDMapper[Api] {
    override def connectionPoolName = "SkinnyResourceServlet"
    override def defaultAlias       = createAlias("api")
    override def extract(rs: WrappedResultSet, n: ResultName[Api]) = new Api(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  object ApisController extends SkinnyResourceServlet {
    override def resourceName      = "api"
    override def resourcesName     = "apis"
    override def model             = Api
    override def resourcesBasePath = "/foo/apis"

    override def createForm = validation(
      createParams,
      paramKey("name") is required & maxLength(64),
      paramKey("url") is required & maxLength(128)
    )
    override def createFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String)

    override def updateForm = validation(
      updateParams,
      paramKey("name") is required & maxLength(64),
      paramKey("url") is maxLength(128)
    )
    override def updateFormStrongParameters = createFormStrongParameters

    override def deleteApiAction = {
      params.getAs[Long](idParamName).foreach(model.deleteById)
      halt(202)
    }
  }

  addServlet(ApisController, "/*")

  it should "have list APIs" in {
    post("/foo/apis.json", "name" -> "Twitter API", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
      header("X-Content-Type-Options") should equal("nosniff")
      header("X-XSS-Protection") should equal("1; mode=block")
      header("X-Frame-Options") should equal("sameorigin")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    get("/foo/apis.json") {
      status should equal(200)
      body should equal("""[{"id":1,"name":"Twitter API","url":"https://dev.twitter.com/"}]""")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    get("/foo/apis.xml") {
      status should equal(200)
      body should startWith("""<?xml version="1.0" encoding="utf-8"?>""")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
  }

  it should "have create API" in {
    post("/foo/apis.xml", "name" -> "Twitter API") {
      status should equal(400)
      body should equal("""<?xml version="1.0" encoding="utf-8"?><apis><url>url is required</url></apis>""")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    post("/foo/apis.json", "name" -> "Twitter API") {
      status should equal(400)
      body should equal("""{"name":[],"url":["url is required"]}""")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    post("/foo/apis.xml", "name" -> "Twitter API", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
      body should equal("")
      header("Location") should equal("/foo/apis/2.xml")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    post("/foo/apis.json", "name" -> "Twitter API", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
      body should equal("")
      header("Location") should equal("/foo/apis/3.json")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
  }

  it should "have update API" in {
    val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
    put(s"/foo/apis/${id}.xml") {
      status should equal(400)
      body should equal("""<?xml version="1.0" encoding="utf-8"?><apis><name>name is required</name></apis>""")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    put(s"/foo/apis/${id}.json") {
      status should equal(400)
      body should equal("""{"name":["name is required"],"url":[]}""")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    put(s"/foo/apis/${id}.json", "name" -> "Twitter API") {
      status should equal(200)
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
      body should equal("")
    }
    Api.findById(id).get.name should equal("Twitter API")
  }

  // delete

  it should "have delete API in XML format" in {
    val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
    delete(s"/foo/apis/${id}.xml") {
      status should equal(202)
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
      body should equal("")
    }
    Api.findById(id).isDefined should equal(false)
  }
  it should "have delete API in JSON format" in {
    val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
    delete(s"/foo/apis/${id}.json") {
      status should equal(202)
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
      body should equal("")
    }
    Api.findById(id).isDefined should equal(false)
  }
  it should "have delete API in HTML format" in {
    val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
    delete(s"/foo/apis/${id}") {
      status should equal(200)
      body should equal("")
    }
    Api.findById(id).isDefined should equal(false)
  }

}
