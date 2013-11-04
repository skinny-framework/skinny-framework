package controller

import skinny._
import skinny.validator._
import model.Company

object CompaniesController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def scalateExtension = "jade"

  override def model = Company
  override def resourcesName = "companies"
  override def resourceName = "company"

  override def createForm = validation(createParams,
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128)
  )
  override def createFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String)

  override def updateParams = Params(params).withDateTime("updatedAt")
  override def updateForm = validation(updateParams,
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128),
    paramKey("updatedAt") is required & dateTimeFormat
  )
  override def updateFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String, "updatedAt" -> ParamType.DateTime)

}
