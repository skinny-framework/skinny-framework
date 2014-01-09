package model

import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import skinny.orm.SkinnyCRUDMapper
import skinny.orm.feature.{ TimestampsFeature, SoftDeleteWithTimestampFeature }

case class Programmer(
    id: Long,
    name: String,
    favoriteNumber: Long,
    companyId: Option[CompanyId] = None,
    company: Option[Company] = None,
    skills: Seq[Skill] = Nil,
    birthday: Option[LocalDate] = None,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None,
    deletedAt: Option[DateTime] = None) {

  def addSkill(skill: Skill)(implicit session: DBSession = ProgrammerSkill.autoSession): Unit = {
    ProgrammerSkill.withColumns { c =>
      ProgrammerSkill.createWithNamedValues(c.programmerId -> id, c.skillId -> skill.id)
    }
  }

  def deleteSkill(skill: Skill)(implicit session: DBSession = Programmer.autoSession): Unit = {
    ProgrammerSkill.withColumns { c =>
      withSQL {
        delete.from(ProgrammerSkill).where.eq(c.programmerId, id).and.eq(c.skillId, skill.id)
      }.update.apply()
    }
  }
}

object Programmer extends SkinnyCRUDMapper[Programmer]
    with TimestampsFeature[Programmer]
    with SoftDeleteWithTimestampFeature[Programmer] {

  override lazy val defaultAlias = createAlias("p")
  override lazy val nameConverters = Map("At$" -> "_timestamp")

  override def extract(rs: WrappedResultSet, p: ResultName[Programmer]): Programmer = new Programmer(
    id = rs.long(p.id),
    name = rs.string(p.name),
    favoriteNumber = rs.long(p.favoriteNumber),
    companyId = rs.longOpt(p.companyId).map(CompanyId),
    birthday = rs.localDateOpt(p.birthday),
    createdAt = rs.dateTime(p.createdAt),
    updatedAt = rs.dateTimeOpt(p.updatedAt)
  )

  private val c = Company.defaultAlias

  belongsToWithJoinCondition[Company](Company, sqls.eq(defaultAlias.companyId, c.id).and.isNull(c.deletedAt), (p, c) => p.copy(company = c)).byDefault

  hasManyThrough[Skill](ProgrammerSkill, Skill, (p, skills) => p.copy(skills = skills)).byDefault

  private val (p, ps) = (Programmer.defaultAlias, ProgrammerSkill.defaultAlias)

  def findNoSkillProgrammers()(implicit session: DBSession = autoSession): List[Programmer] = extract {
    withSQL {
      defaultSelectQuery.where.notIn(p.id,
        select(sqls.distinct(ps.programmerId)).from(ProgrammerSkill as ps)).and(defaultScopeForUpdateOperations)
    }
  }.list.apply()

  def deleteByIdCascade(id: Long): Int = DB localTx { implicit s =>
    ProgrammerSkill.withColumns { c =>
      withSQL(delete.from(ProgrammerSkill).where.eq(c.programmerId, id)).update.apply()
    }
    deleteById(id)
  }

}
