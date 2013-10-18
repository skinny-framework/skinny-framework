package controller

import skinny._
import skinny.validator._
import model.Company

object CompaniesController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Company
  override def resourcesName = "companies"
  override def resourceName = "company"

  override def createForm = validation(
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128)
  )
  override def createFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String)

  override def updateForm = validation(
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("url") is maxLength(128)
  )
  override def updateFormStrongParameters = Seq("name" -> ParamType.String, "url" -> ParamType.String)
}
