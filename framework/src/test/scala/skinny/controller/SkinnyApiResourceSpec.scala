package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.orm.SkinnyCRUDMapper
import skinny.validator._
import skinny.ParamType

class SkinnyApiResourceSpec extends ScalatraFlatSpec {

  behavior of "SkinnyApiResource"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add("SkinnyApiResource", "jdbc:h2:mem:SkinnyApiResource", "", "")
  NamedDB("SkinnyApiResource").localTx { implicit s =>
    sql"create table api (id serial primary key, name varchar(64) not null, url varchar(128) not null);".execute
      .apply()
  }

  case class Api(id: Long, name: String, url: String)
  object Api extends SkinnyCRUDMapper[Api] {
    override def connectionPoolName = "SkinnyApiResource"
    override def defaultAlias       = createAlias("api")
    override def extract(rs: WrappedResultSet, n: ResultName[Api]) = new Api(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  object ApisController extends SkinnyApiResource {
    override def resourceName      = "api"
    override def resourcesName     = "apis"
    override def model             = Api
    override def resourcesBasePath = "/bar/apis"

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
  }

  addFilter(ApisController, "/*")

  it should "have list APIs" in {
    get("/bar/apis.json") {
      status should equal(200)
      header("X-Content-Type-Options") should equal("nosniff")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    get("/bar/apis.xml") {
      status should equal(200)
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
  }

  it should "have create API" in {
    post("/bar/apis.xml", "name" -> "Twitter API") {
      status should equal(400)
      body should equal("""<?xml version="1.0" encoding="utf-8"?><apis><url>url is required</url></apis>""")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    post("/bar/apis.json", "name" -> "Twitter APi") {
      status should equal(400)
      body should equal("""{"name":[],"url":["url is required"]}""")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    post("/bar/apis.xml", "name" -> "Twitter APi", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
      header("Location") should equal("/bar/apis/1.xml")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    post("/bar/apis.json", "name" -> "Twitter APi", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
      header("Location") should equal("/bar/apis/2.json")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
  }

  it should "have update API" in {
    val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
    put(s"/bar/apis/${id}.xml") {
      body should equal("""<?xml version="1.0" encoding="utf-8"?><apis><name>name is required</name></apis>""")
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
    put(s"/bar/apis/${id}.json") {
      status should equal(400)
      body should equal("""{"name":["name is required"],"url":[]}""")
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    put(s"/bar/apis/${id}.json", "name" -> "Twitter API") {
      status should equal(200)
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    Api.findById(id).get.name should equal("Twitter API")

    put(s"/bar/apis/dummy.json", "name" -> "Twitter API") {
      status should equal(404)
      header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
    }
    put(s"/bar/apis/dummy.xml", "name" -> "Twitter API") {
      status should equal(404)
      header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
    }
  }

  it should "have delete API" in {
    {
      val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
      delete(s"/bar/apis/${id}.xml") {
        status should equal(200)
        header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
        body should equal("")
      }
      Api.findById(id).isDefined should equal(false)
    }

    {
      val id = Api.createWithAttributes("name" -> "Twitter", "url" -> "https://dev.twitter.com")
      delete(s"/bar/apis/${id}.json") {
        status should equal(200)
        header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
        body should equal("")
      }
      Api.findById(id).isDefined should equal(false)

      delete(s"/bar/apis/dummy.json") {
        status should equal(404)
        header("Content-Type") should fullyMatch regex ("application/json;\\s*charset=utf-8")
        body should not equal ("")
      }
      delete(s"/bar/apis/dummy.xml") {
        status should equal(404)
        header("Content-Type") should fullyMatch regex ("application/xml;\\s*charset=utf-8")
        body should not equal ("")
      }
    }
  }

}
