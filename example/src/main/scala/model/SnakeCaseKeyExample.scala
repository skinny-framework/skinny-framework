package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class SnakeCaseKeyExample(
  id: Long,
  firstName: String,
  luckeyNumber: Int,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None)

object SnakeCaseKeyExample extends SkinnyCRUDMapper[SnakeCaseKeyExample] with TimestampsFeature[SnakeCaseKeyExample] {
  override val tableName = "snake_case_key_examples"
  override val defaultAlias = createAlias("s")

  override def extract(rs: WrappedResultSet, rn: ResultName[SnakeCaseKeyExample]): SnakeCaseKeyExample = autoConstruct(rs, rn)
}
