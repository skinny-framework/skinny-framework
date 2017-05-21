package controller

import skinny._
import skinny.validator._
import model._

class ProgrammersController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model         = Programmer
  override def resourcesName = "programmers"
  override def resourceName  = "programmer"

  override def itemsName = resourcesName
  override def itemName  = resourceName

  beforeAction(only = Seq('index, 'indexWithSlash, 'new, 'create, 'createWithSlash, 'edit, 'update)) {
    set("companies", Company.findAll())
    set("skills", Skill.findAll())
  }

  override def createParams =
    Params(params + ("hashedPassword" -> params.as[PlainPassword]("plainTextPassword").hash("dummy salt")))
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

  def addSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer   <- Programmer.findById(programmerId)
      skillId      <- params.getAs[Long]("skillId")
      skill        <- Skill.findById(skillId)
    } yield {
      try programmer.addSkill(skill)
      catch { case scala.util.control.NonFatal(_) => halt(409) }
    }) getOrElse halt(404)
  }

  def deleteSkill = {
    (for {
      programmerId <- params.getAs[Long]("programmerId")
      programmer   <- Programmer.findById(programmerId)
      skillId      <- params.getAs[Long]("skillId")
      skill        <- Skill.findById(skillId)
    } yield {
      programmer.deleteSkill(skill)
    }) getOrElse haltWithBody(404)
  }

  def joinCompany = {
    (for {
      companyId    <- params.getAs[Long]("companyId").map(CompanyId.apply)
      company      <- Company.findById(companyId)
      programmerId <- params.getAs[Long]("programmerId")
      programmer   <- Programmer.findById(programmerId)
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
      programmer   <- Programmer.findById(programmerId)
    } yield {
      Programmer.withColumns { c =>
        Programmer.updateById(programmerId).withNamedValues(c.companyId -> None)
      }
      status = 200
    }) getOrElse haltWithBody(404)
  }

  private object ProgrammerParamType {
    val HashedPassword = ParamType {
      case v: HashedPassword => v.value
    }
  }

}
