package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.TimestampsFeature

case class ServiceSetting(
  id: Long,
  maximumAccounts: Long,
  serviceId: Long,
  service: Option[Service] = None,
  createdAt: DateTime,
  updatedAt: DateTime)

object ServiceSetting extends SkinnyCRUDMapper[ServiceSetting] with TimestampsFeature[ServiceSetting] {
  override val connectionPoolName = 'service
  override val tableName = "service_settings"
  override def defaultAlias = createAlias("ss")

  override def extract(rs: WrappedResultSet, n: ResultName[ServiceSetting]) = new ServiceSetting(
    id = rs.get(n.id),
    maximumAccounts = rs.get(n.maximumAccounts),
    serviceId = rs.get(n.serviceId),
    createdAt = rs.get(n.createdAt),
    updatedAt = rs.get(n.updatedAt)
  )

}