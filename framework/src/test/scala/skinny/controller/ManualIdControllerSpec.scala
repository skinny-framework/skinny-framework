package skinny.controller

import org.scalatra.test.scalatest._
import scalikejdbc._
import skinny.{ ParamType, Routes }
import skinny.orm._
import skinny.validator._

class ManualIdControllerSpec extends ScalatraFlatSpec {

  behavior of "SkinnyController with manual ID"

  Class.forName("org.h2.Driver")
  ConnectionPool.add("ManualIdController", "jdbc:h2:mem:ManualIdController", "", "")

  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)

  NamedDB("ManualIdController").localTx { implicit s =>
    sql"create table company (id serial primary key, name varchar(64), url varchar(128));".execute.apply()
  }

  case class Company(id: Long, name: String, url: String)

  object Company extends SkinnyCRUDMapper[Company] {
    override def connectionPoolName = "ManualIdController"

    override def defaultAlias               = createAlias("c")
    override def useAutoIncrementPrimaryKey = false

    override def rawValueToId(value: Any) = value.toString.toLong
    override def idToRawValue(id: Long)   = id

    override def extract(rs: WrappedResultSet, n: ResultName[Company]) = new Company(
      id = rs.get(n.id),
      name = rs.get(n.name),
      url = rs.get(n.url)
    )
  }

  class CompaniesController extends SkinnyResource {
    override protected def resourcesName = "companies"
    override protected def resourceName  = "company"
    override protected def model         = Company

    override protected def createFormStrongParameters = Seq(
      "id"   -> ParamType.Long,
      "name" -> ParamType.String,
      "url"  -> ParamType.String
    )
    override protected def createForm = validation(
      createParams,
      paramKey("id") is required & numeric,
      paramKey("name") is required & maxLength(64),
      paramKey("url") is required & maxLength(128)
    )
    override protected def updateFormStrongParameters = createFormStrongParameters
    override protected def updateForm = validation(
      updateParams,
      paramKey("id") is required & numeric,
      paramKey("name") is required & maxLength(64),
      paramKey("url") is required & maxLength(128)
    )
  }
  val controller = new CompaniesController with Routes {}

  addFilter(controller, "/*")

  /*
java.lang.ClassCastException: scala.Some cannot be cast to java.lang.Long
	at scala.runtime.BoxesRunTime.unboxToLong(BoxesRunTime.java:109)
	at skinny.controller.ManualIdControllerSpec$Company$.idToRawValue(ManualIdControllerSpec.scala:24)
	at skinny.controller.SkinnyResourceActions$$anonfun$createResource$1.apply(SkinnyResourceActions.scala:148)
	at skinny.controller.SkinnyControllerBase$class.withFormat(SkinnyControllerBase.scala:101)
	at skinny.controller.SkinnyController.withFormat(SkinnyController.scala:6)
	at skinny.controller.SkinnyResourceActions$class.createResource(SkinnyResourceActions.scala:135)
	at skinny.controller.ManualIdControllerSpec$CompaniesController.createResource(ManualIdControllerSpec.scala:37)
	at skinny.controller.SkinnyResourceRoutes$$anonfun$5.apply(SkinnyResourceRoutes.scala:28)
	at org.scalatra.ScalatraBase$class.org$scalatra$ScalatraBase$$liftAction(ScalatraBase.scala:270)
	at org.scalatra.ScalatraBase$$anonfun$invoke$1.apply(ScalatraBase.scala:265)
	at org.scalatra.ScalatraBase$$anonfun$invoke$1.apply(ScalatraBase.scala:265)
	at org.scalatra.ApiFormats$class.withRouteMultiParams(ApiFormats.scala:178)
   */
  it should "create a resource via API without errors" in {
    post("/companies.json", "id" -> "1", "name" -> "CompanyName", "url" -> "http://www.example.com/") {
      if (status != 201) println(body)
      status should equal(201)
    }
  }
  it should "create a resource without errors" in {
    post("/companies", "id" -> "2", "name" -> "CompanyName", "url" -> "http://www.example.com/") {
      if (status != 302) println(body)
      status should equal(302)
    }
  }

}
