package model

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import skinny.orm.feature._

case class Company(
  id: Long,
  name: String,
  url: Option[String] = None,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None,
  deletedAt: Option[DateTime] = None)

object Company extends SkinnyCRUDMapper[Company]
    with TimestampsFeature[Company]
    with SoftDeleteWithTimestampFeature[Company] {

  override val defaultAlias = createAlias("c")

  override def extract(rs: WrappedResultSet, c: ResultName[Company]): Company = new Company(
    id = rs.long(c.id),
    name = rs.string(c.name),
    url = rs.stringOpt(c.url),
    createdAt = rs.dateTime(c.createdAt),
    updatedAt = rs.dateTimeOpt(c.updatedAt)
  )

}
