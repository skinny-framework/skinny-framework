package model

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import skinny.orm.feature.{ TimestampsFeature, SoftDeleteWithTimestampFeature }

case class Skill(
  id: Long,
  name: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None)

object Skill extends SkinnyCRUDMapper[Skill]
    with TimestampsFeature[Skill]
    with SoftDeleteWithTimestampFeature[Skill] {

  override val defaultAlias = createAlias("s")

  override def extract(rs: WrappedResultSet, s: ResultName[Skill]): Skill = new Skill(
    id = rs.long(s.id),
    name = rs.string(s.name),
    createdAt = rs.dateTime(s.createdAt),
    updatedAt = rs.dateTimeOpt(s.updatedAt)
  )
}
