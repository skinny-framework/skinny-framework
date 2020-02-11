package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._
import skinny.orm.feature.{ SoftDeleteWithTimestampFeature, TimestampsFeature }

case class Application(
    id: Long,
    name: String,
    serviceNo: Long,
    service: Option[Service] = None,
    createdAt: DateTime,
    updatedAt: DateTime
)

object Application
    extends SkinnyCRUDMapper[Application]
    with TimestampsFeature[Application]
    with SoftDeleteWithTimestampFeature[Application] {

  override val connectionPoolName = "service"
  override val tableName          = "applications"
  override def defaultAlias       = createAlias("a")

  override def extract(rs: WrappedResultSet, n: ResultName[Application]) = autoConstruct(rs, n, "service")

  val service = belongsTo[Service](
    right = Service,
    merge = (app, service) => app.copy(service = service)
  )

}
