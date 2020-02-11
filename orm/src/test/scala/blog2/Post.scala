package blog2

import skinny.orm.SkinnyCRUDMapper
import skinny.orm.feature.TimestampsFeature
import scalikejdbc._
import org.joda.time._

case class Post(
    id: Long,
    title: String,
    body: String,
    tags: Seq[Tag] = Nil,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None
)

object Post extends SkinnyCRUDMapper[Post] with TimestampsFeature[Post] {
  override val connectionPoolName = "blog2"
  override val tableName          = "posts"
  override val defaultAlias       = createAlias("p")

  val tagsRef = hasManyThrough[Tag](
    through = PostTag,
    many = Tag,
    merge = (p, t) => p.copy(tags = t)
  ).byDefault

  override def extract(rs: WrappedResultSet, rn: ResultName[Post]): Post = new Post(
    id = rs.get(rn.id),
    title = rs.get(rn.title),
    body = rs.get(rn.body),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
