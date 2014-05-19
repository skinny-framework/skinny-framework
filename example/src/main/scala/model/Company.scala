package model

import scalikejdbc._
import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapperWithId
import skinny.orm.feature._

case class Company(
  id: CompanyId,
  name: String,
  url: Option[String] = None,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None,
  deletedAt: Option[DateTime] = None)

object Company extends SkinnyCRUDMapperWithId[CompanyId, Company]
    with TimestampsFeatureWithId[CompanyId, Company]
    with SoftDeleteWithTimestampFeatureWithId[CompanyId, Company] {

  override val defaultAlias = createAlias("c")

  def idToRawValue(id: CompanyId) = id.value
  def rawValueToId(value: Any) = CompanyId(value.toString.toLong)

  override def extract(rs: WrappedResultSet, c: ResultName[Company]): Company = new Company(
    id = CompanyId(rs.long(c.id)),
    name = rs.string(c.name),
    url = rs.stringOpt(c.url),
    createdAt = rs.dateTime(c.createdAt),
    updatedAt = rs.dateTimeOpt(c.updatedAt)
  )

}
