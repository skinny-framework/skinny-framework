package whitespace

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Tag(
    id: Long,
    name: String,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None
)

object Tag extends SkinnyCRUDMapper[Tag] with TimestampsFeature[Tag] {
  override val connectionPoolName = "ws"
  override val tableName          = "tags"
  override val defaultAlias       = createAlias("t")

  override def extract(rs: WrappedResultSet, rn: ResultName[Tag]): Tag = new Tag(
    id = rs.get(rn.id),
    name = rs.get(rn.name),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
