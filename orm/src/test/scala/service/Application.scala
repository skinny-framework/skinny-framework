package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.TimestampsFeature

case class Application(
  id: Long,
  name: String,
  serviceId: Long,
  service: Option[Service] = None,
  createdAt: DateTime,
  updatedAt: DateTime)

object Application extends SkinnyCRUDMapper[Application] with TimestampsFeature[Application] {
  override val connectionPoolName = 'service
  override val tableName = "applications"
  override def defaultAlias = createAlias("a")

  override def extract(rs: WrappedResultSet, n: ResultName[Application]) = new Application(
    id = rs.get(n.id),
    name = rs.get(n.name),
    serviceId = rs.get(n.serviceId),
    createdAt = rs.get(n.createdAt),
    updatedAt = rs.get(n.updatedAt)
  )

  val service = belongsTo[Service](
    right = Service,
    merge = (app, service) => app.copy(service = service)
  )

}