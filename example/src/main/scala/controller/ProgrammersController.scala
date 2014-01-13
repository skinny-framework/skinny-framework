package controller

import skinny._
import skinny.validator._
import model._

class ProgrammersController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Programmer
  override def resourcesName = "programmers"
  override def resourceName = "programmer"

  beforeAction(only = Seq('index, 'indexWithSlash, 'new, 'create, 'createWithSlash, 'edit, 'update)) {
    set("companies", Company.findAll())
    set("skills", Skill.findAll())
  }

  override def createForm = validation(createParams,
    paramKey("name") is required & maxLength(64),
    paramKey("favoriteNumber") is required & numeric,
    paramKey("companyId") is numeric
  )
  override def createFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "favoriteNumber" -> ParamType.Long,
    "companyId" -> ParamType.Long)

  override def updateParams = Params(params).withDate("birthday")
  override def updateForm = validation(updateParams,
    paramKey("id") is required,
    paramKey("name") is required & maxLength(64),
    paramKey("favoriteNumber") is required & numeric,
    paramKey("companyId") is numeric
  )
  override def updateFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "favoriteNumber" -> ParamType.Long,
    "companyId" -> ParamType.Long,
    "birthday" -> ParamType.LocalDate)

  override def doDestroy(id: Long) = model.deleteByIdCascade(id)

  def addSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
      skillId <- params.getAs[Long]("skillId")
      skill <- Skill.findById(skillId)
    } yield {
      try programmer.addSkill(skill)
      catch { case e: Exception => halt(409) }
    }) getOrElse halt(404)
  }

  def deleteSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
      skillId <- params.getAs[Long]("skillId")
      skill <- Skill.findById(skillId)
    } yield {
      programmer.deleteSkill(skill)
    }) getOrElse haltWithBody(404)
  }

  def joinCompany = {
    (for {
      companyId <- params.getAs[Long]("companyId").map(CompanyId)
      company <- Company.findById(companyId)
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
    } yield {
      Programmer.withColumns { c =>
        Programmer.updateById(programmerId).withNamedValues(c.companyId -> company.id.value)
      }
      status = 200
    }) getOrElse haltWithBody(404)
  }

  def leaveCompany = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer <- Programmer.findById(programmerId)
    } yield {
      Programmer.withColumns { c =>
        Programmer.updateById(programmerId).withNamedValues(c.companyId -> None)
      }
      status = 200
    }) getOrElse haltWithBody(404)
  }

}
