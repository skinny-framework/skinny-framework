package controller

import org.json4s._
import skinny._
import skinny.controller.SkinnyApiResource
import skinny.controller.feature.AngularXHRServerFeature
import skinny.validator._
import model._

object AngularXHRProgrammersController extends AngularXHRProgrammersController

class AngularXHRProgrammersController extends SkinnyApiResource with AngularXHRServerFeature {

  // ----
  // XSRF protection
  // https://docs.angularjs.org/api/ng/service/$http#cross-site-request-forgery-xsrf-protection
  protectFromForgery()

  // ----
  // JSON serialization

  // default: "companyId":{"value":1},"company":{"id":{"value":1},"name"
  // with serializers: "companyId":1,"company":{"id":1,

  class CompanyIdSerializer extends Serializer[CompanyId] {
    val CompanyIdClass = classOf[CompanyId]
    override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), CompanyId] = {
      case (TypeInfo(CompanyIdClass, _), json) =>
        json match {
          case JObject(JField("companyId", JDecimal(id)) :: _) => CompanyId(id.toLong)
          case x                                               => throw new MappingException(s"Can't convert ${x} to companyId")
        }
    }
    import JsonDSL._
    override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case x: CompanyId => x.value
    }
  }
  override protected implicit val jsonFormats: Formats = {
    DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all + new CompanyIdSerializer
  }

  override def model         = Programmer
  override def resourcesName = "programmers"
  override def resourceName  = "programmer"

  override def resourcesBasePath = s"/angular/${resourcesName}"

  override def createParams =
    Params(
      params +
      ("hashedPassword" -> params.getAs[PlainPassword]("plainTextPassword").map(_.hash("dummy salt")))
    )
  override def createForm = validation(
    createParams,
    paramKey("name") is required & maxLength(64),
    paramKey("favoriteNumber") is required & numeric,
    paramKey("companyId") is numeric,
    paramKey("plainTextPassword") is required & minLength(8)
  )
  override def createFormStrongParameters = Seq(
    "name"           -> ParamType.String,
    "favoriteNumber" -> ParamType.Long,
    "companyId"      -> ParamType.Long,
    "hashedPassword" -> ProgrammerParamType.HashedPassword
  )

  override def updateParams = Params(params).withDate("birthday")
  override def updateForm = validation(
    updateParams,
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("favoriteNumber") is required & numeric,
    paramKey("companyId") is numeric
  )
  override def updateFormStrongParameters = Seq(
    "name"           -> ParamType.String,
    "favoriteNumber" -> ParamType.Long,
    "companyId"      -> ParamType.Long,
    "birthday"       -> ParamType.LocalDate
  )

  override def doDestroy(id: Long) = model.deleteByIdCascade(id)

  private object ProgrammerParamType {
    val HashedPassword = ParamType {
      case v: HashedPassword => v.value
    }
  }

}
