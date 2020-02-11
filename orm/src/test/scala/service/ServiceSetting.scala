package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._
import skinny.orm.feature.TimestampsFeature

case class ServiceSetting(
    id: Long,
    maximumAccounts: Long,
    serviceNo: Long,
    service: Option[Service] = None,
    createdAt: DateTime,
    updatedAt: DateTime
)

object ServiceSetting extends SkinnyCRUDMapper[ServiceSetting] with TimestampsFeature[ServiceSetting] {
  override val connectionPoolName = "service"
  override val tableName          = "service_settings"
  override def defaultAlias       = createAlias("ss")

  override def extract(rs: WrappedResultSet, n: ResultName[ServiceSetting]) = autoConstruct(rs, n, "service")

}
