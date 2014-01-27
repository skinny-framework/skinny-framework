package controller

import skinny._
import skinny.validator._
import model._
import skinny.filter._

object SkillsController extends SkinnyResource with ApplicationController
    with TxPerRequestFilter with SkinnySessionFilter {

  protectFromForgery()

  override def model = Skill
  override def resourcesName = "skills"
  override def resourceName = "skill"

  override def createForm = validation(createParams, paramKey("name") is required & maxLength(64))
  override def createFormStrongParameters = Seq("name" -> ParamType.String)

  override def updateForm = createForm
  override def updateFormStrongParameters = Seq("name" -> ParamType.String)

  override def doDestroy(id: Long) = model.deleteByIdCascade(id)

  def urlSample = url(SkillsController.indexUrl, "page" -> "1")

}
