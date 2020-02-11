package whitespace

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class PostTag(
    id: Long,
    tagId: Int,
    postId: Int,
    createdAt: DateTime
)

object PostTag extends SkinnyJoinTable[PostTag] {
  override val connectionPoolName = "ws"
  override val tableName          = "posts_tags"
  override val defaultAlias       = createAlias("pt")

  override def extract(rs: WrappedResultSet, rn: ResultName[PostTag]): PostTag = new PostTag(
    id = rs.get(rn.id),
    tagId = rs.get(rn.tagId),
    postId = rs.get(rn.postId),
    createdAt = rs.get(rn.createdAt)
  )
}
