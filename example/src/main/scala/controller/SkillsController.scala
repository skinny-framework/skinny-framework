package controller

import skinny._
import skinny.validator._
import model._

object SkillsController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override lazy val scalateExtension: String = "scaml"

  override def model = Skill
  override def resourcesName = "skills"
  override def resourceName = "skill"

  override def createForm = validation(paramKey("name") is required & maxLength(64))
  override def createFormStrongParameters = Seq("name" -> ParamType.String)

  override def updateForm = createForm
  override def updateFormStrongParameters = Seq("name" -> ParamType.String)

  override def doDestroy(id: Long) = model.deleteByIdCascade(id)

}
