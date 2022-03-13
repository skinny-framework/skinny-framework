package blog

import skinny.orm._
import scalikejdbc._
import org.joda.time._

case class PostTag(
    id: Long,
    tagId: Int,
    postId: Int,
    createdAt: DateTime
)

object PostTag extends SkinnyJoinTable[PostTag] {
  override val connectionPoolName = "blog"
  override val tableName          = "posts_tags"
  override val defaultAlias       = createAlias("pt")

  override def extract(rs: WrappedResultSet, rn: ResultName[PostTag]): PostTag = autoConstruct(rs, rn)
}
