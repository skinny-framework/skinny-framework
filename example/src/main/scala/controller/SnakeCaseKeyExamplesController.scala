package controller

import skinny._
import skinny.validator._
import model.SnakeCaseKeyExample

object SnakeCaseKeyExamplesController extends SkinnyResource {
  protectFromForgery()

  override def resourcesName = "snakeCaseKeyExamples"
  override def resourceName = "snakeCaseKeyExample"

  override def resourcesBasePath = "/snake_case_key_examples"
  override def useSnakeCasedParamKeys = true

  override def model = SnakeCaseKeyExample

  override def createForm = validation(createParams,
    paramKey("first_name") is required & maxLength(512),
    paramKey("luckey_number") is required & numeric & intValue
  )
  override def createParams = Params(params)

  override def createFormStrongParameters = Seq(
    "first_name" -> ParamType.String,
    "luckey_number" -> ParamType.Int
  )

  override def updateForm = validation(updateParams,
    paramKey("first_name") is required & maxLength(512),
    paramKey("luckey_number") is required & numeric & intValue
  )
  override def updateParams = Params(params)

  override def updateFormStrongParameters = Seq(
    "first_name" -> ParamType.String,
    "luckey_number" -> ParamType.Int
  )

}
