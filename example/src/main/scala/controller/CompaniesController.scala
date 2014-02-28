package controller

import skinny._
import skinny.validator._
import model._
import skinny.filter._

object CompaniesController extends SkinnyResourceWithId[CompanyId] with ApplicationController with SkinnySessionFilter {
  protectFromForgery()

  // TODO remove in 1.0.0
  // just for backward compatibility for 0.9.27 or older
  override def scalateExtension = "jade"

  implicit override val scalatraParamsIdTypeConverter = new skinny.TypeConverter[String, CompanyId] {
    def apply(s: String): Option[CompanyId] = Option(s).map(model.rawValueToId)
  }

  override def model = Company
  override def resourcesName = "companies"
  override def resourceName = "company"

  override def createParams = Params(params).withDateTime("updatedAt")
  override def createForm = validation(createParams,
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128),
    paramKey("updatedAt") is required & dateTimeFormat
  )
  override def createFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "url" -> ParamType.String,
    "updatedAt" -> ParamType.DateTime)

  override def updateParams = Params(params).withDateTime("updatedAt")
  override def updateForm = validation(updateParams,
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128),
    paramKey("updatedAt") is required & dateTimeFormat
  )
  override def updateFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "url" -> ParamType.String,
    "updatedAt" -> ParamType.DateTime)

}
