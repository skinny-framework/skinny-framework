package model

import scalikejdbc._
import skinny.orm.SkinnyCRUDMapper

case class Comment(id: Long, author: String, text: String)

object Comment extends SkinnyCRUDMapper[Comment] {
  override def tableName = "comments"
  override val defaultAlias = createAlias("cmt")

  override def extract(rs: WrappedResultSet, s: ResultName[Comment]): Comment = autoConstruct(rs, s)
}
