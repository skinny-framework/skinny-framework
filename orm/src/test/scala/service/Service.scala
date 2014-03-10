package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.{ SoftDeleteWithTimestampFeature, TimestampsFeature }

case class Service(
  id: Long,
  name: String,
  createdAt: DateTime,
  updatedAt: DateTime,
  applications: Seq[Application] = Nil,
  settings: Seq[ServiceSetting] = Nil)

object Service extends SkinnyCRUDMapper[Service] with TimestampsFeature[Service] {

  override val connectionPoolName = 'service
  override val tableName = "services"
  override def defaultAlias = createAlias("s")

  override def extract(rs: WrappedResultSet, n: ResultName[Service]) = new Service(
    id = rs.get(n.id),
    name = rs.get(n.name),
    createdAt = rs.get(n.createdAt),
    updatedAt = rs.get(n.updatedAt)
  )

  val serviceSettings = hasMany[ServiceSetting](
    many = ServiceSetting -> ServiceSetting.defaultAlias,
    on = (s, ss) => sqls.eq(s.id, ss.serviceId),
    merge = (s, settings) => s.copy(settings = settings)
  )

  val applications = hasMany[Application](
    many = Application -> Application.defaultAlias,
    on = (s, app) => sqls.eq(s.id, app.serviceId),
    merge = (s, applications) => s.copy(applications = applications)
  )

}