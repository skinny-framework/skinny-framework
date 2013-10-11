package model

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper

case class Skill(id: Long, name: String)

object Skill extends SkinnyCRUDMapper[Skill] {
  override val defaultAlias = createAlias("s")

  override def extract(rs: WrappedResultSet, s: ResultName[Skill]): Skill = new Skill(
    id = rs.long(s.id), name = rs.string(s.name))

  def deleteByIdCascade(id: Long): Int = DB localTx { implicit s =>
    ProgrammerSkill.withColumns { c =>
      withSQL(delete.from(ProgrammerSkill).where.eq(c.skillId, id)).update.apply()
    }
    deleteById(id)
  }
}
