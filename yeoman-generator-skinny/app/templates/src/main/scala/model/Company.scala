package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

case class Company(id: Long, name: String, url: Option[String], createdAt: DateTime, updatedAt: Option[DateTime])

object Company extends SkinnyCRUDMapper[Company] with TimestampsFeature[Company] {
  override val tableName = "companies"
  override val defaultAlias = createAlias("c")

  override def extract(rs: WrappedResultSet, c: ResultName[Company]): Company = new Company(
    id = rs.long(c.id),
    name = rs.string(c.name),
    url = rs.stringOpt(c.url),
    createdAt = rs.dateTime(c.createdAt),
    updatedAt = rs.dateTimeOpt(c.updatedAt)
  )

}

