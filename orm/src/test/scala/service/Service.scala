package service

import org.joda.time.DateTime
import skinny.orm.SkinnyCRUDMapper
import scalikejdbc._
import skinny.orm.feature.TimestampsFeature

case class Service(
    no: Long,
    name: String,
    createdAt: DateTime,
    updatedAt: DateTime,
    applications: Seq[Application] = Nil,
    settings: Seq[ServiceSetting] = Nil
)

object Service extends SkinnyCRUDMapper[Service] with TimestampsFeature[Service] {

  override val connectionPoolName  = "service"
  override val tableName           = "services"
  override def defaultAlias        = createAlias("s")
  override def primaryKeyFieldName = "no"

  override def extract(rs: WrappedResultSet, n: ResultName[Service]) =
    autoConstruct(rs, n, "applications", "settings")

  val serviceSettings = hasMany[ServiceSetting](
    many = ServiceSetting -> ServiceSetting.defaultAlias,
    on = (s, ss) => sqls.eq(s.no, ss.serviceNo),
    merge = (s, settings) => s.copy(settings = settings)
  )

  val applications = hasMany[Application](
    many = Application -> Application.defaultAlias,
    on = (s, app) => sqls.eq(s.no, app.serviceNo),
    merge = (s, applications) => s.copy(applications = applications)
  )

}
