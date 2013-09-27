package controller

import skinny._
import skinny.validator._
import model._

object SkillsController extends SkinnyResource {
  protectFromForgery()

  override lazy val scalateExtension: String = "scaml"

  override def skinnyCRUDMapper = Skill
  override def resourcesName = "skills"
  override def resourceName = "skill"

  override def createForm = validation(paramKey("name") is required & maxLength(64))
  override def createFormStrongParameters = Seq("name" -> ParamType.String)

  override def updateForm = createForm
  override def updateFormStrongParameters = Seq("name" -> ParamType.String)

  override def destroyResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    skinnyCRUDMapper.findById(id).map { m =>
      skinnyCRUDMapper.deleteByIdCascade(id)
      status = 200
    } getOrElse haltWithBody(404)
  }

}
