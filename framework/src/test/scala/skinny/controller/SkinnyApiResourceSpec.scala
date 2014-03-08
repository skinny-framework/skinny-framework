package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._, SQLInterpolation._
import skinny.orm.SkinnyCRUDMapper
import skinny.validator._
import skinny.ParamType

class SkinnyApiResourceSpec extends ScalatraFlatSpec {

  behavior of "SkinnyApiResource"

  Class.forName("org.h2.Driver")
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  ConnectionPool.add('SkinnyApiResource, "jdbc:h2:mem:SkinnyApiResource", "", "")
  NamedDB('SkinnyApiResource).localTx { implicit s =>
    sql"create table api (id serial primary key, name varchar(64) not null, url varchar(128) not null);"
      .execute.apply()
  }

  case class Api(id: Long, name: String, url: String)
  object Api extends SkinnyCRUDMapper[Api] {
    override def connectionPoolName = 'SkinnyApiResource
    override def defaultAlias = createAlias("api")
    override def extract(rs: WrappedResultSet, n: ResultName[Api]) = new Api(
      id = rs.get(n.id), name = rs.get(n.name), url = rs.get(n.url))
  }

  object ApisController extends SkinnyApiResource {
    override def resourceName = "api"
    override def resourcesName = "apis"
    override def model = Api
    override def resourcesBasePath = "/api/apis"

    override def createForm = validation(createParams,
      paramKey("name") is required & maxLength(64),
      paramKey("url") is required & maxLength(128))
    override def createFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String)

    override def updateForm = validation(updateParams,
      paramKey("name") is maxLength(64),
      paramKey("url") is maxLength(128))
    override def updateFormStrongParameters = createFormStrongParameters
  }

  addFilter(ApisController, "/*")

  it should "have list APIs" in {
    get("/api/apis.json") {
      status should equal(200)
    }
    get("/api/apis.xml") {
      status should equal(200)
    }
  }

  it should "have create API" in {
    post("/api/apis.json", "name" -> "Twitter APi") {
      status should equal(400)
      body should equal("""{"name":[],"url":["url is required"]}""")
    }
    post("/api/apis.json", "name" -> "Twitter APi", "url" -> "https://dev.twitter.com/") {
      status should equal(201)
    }
  }

  it should "have update API" in {
    val id = Api.createWithAttributes('name -> "Twitter", 'url -> "https://dev.twitter.com")
    put(s"/api/apis/${id}.json", "name" -> "Twitter API") {
      status should equal(200)
    }
    Api.findById(id).get.name should equal("Twitter API")
  }

  it should "have delete API" in {
    val id = Api.createWithAttributes('name -> "Twitter", 'url -> "https://dev.twitter.com")
    delete(s"/api/apis/${id}.json") {
      status should equal(200)
    }
    Api.findById(id).isDefined should equal(false)
  }

}
