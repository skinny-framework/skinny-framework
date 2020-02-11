package whitespace

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Post(
    id: Long,
    title: String,
    body: String,
    viewCount: Long,
    tags: Seq[Tag] = Nil,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None
)

object Post extends SkinnyCRUDMapper[Post] with TimestampsFeature[Post] {
  override val connectionPoolName = "ws"
  override val tableName          = "posts"
  override val defaultAlias       = createAlias("p")

  val tagsRef = hasManyThrough[Tag](
    through = PostTag,
    many = Tag,
    merge = (p, t) => p.copy(tags = t)
  ) // .byDefault

  override def extract(rs: WrappedResultSet, rn: ResultName[Post]): Post = new Post(
    id = rs.get(rn.id),
    title = rs.get(rn.title),
    body = rs.get(rn.body),
    viewCount = rs.get(rn.viewCount),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
