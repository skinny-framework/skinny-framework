package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.orm.exception.OptimisticLockException
import org.joda.time.DateTime

trait OptimisticLockWithTimestampFeature[Entity] extends CRUDFeature[Entity] {

  val lockTimestampName = "lockTimestamp"

  protected def byIdAndTimestamp(id: Long, timestamp: Option[DateTime]) = timestamp.map { t =>
    sqls.eq(column.field(primaryKeyName), id).and.eq(column.field(lockTimestampName), t)
  }.getOrElse {
    sqls.eq(column.field(primaryKeyName), id).and.isNull(column.field(lockTimestampName))
  }

  def updateByIdAndTimestamp(id: Long, timestamp: Option[DateTime]) = {
    updateBy(byIdAndTimestamp(id, timestamp))
  }

  def updateByIdAndTimestamp(id: Long, timestamp: DateTime) = {
    updateBy(byIdAndTimestamp(id, Option(timestamp)))
  }

  override def updateBy(where: SQLSyntax): UpdateOperationBuilder = new UpdateOperationBuilderWithVersion(this, where)

  class UpdateOperationBuilderWithVersion(self: CRUDFeature[Entity], where: SQLSyntax) extends UpdateOperationBuilder(self, where) {
    private[this] val c = defaultAlias.support.column.field(lockTimestampName)
    addUpdateSQLPart(sqls"${c} = ${sqls.currentTimestamp}")

    override def onComplete(count: Int): Int = {
      super.onComplete(count)
      if (count == 0) {
        throw new OptimisticLockException(
          s"Conflict ${lockTimestampName} is detected (condition: '${where.value}', ${where.parameters.mkString(",")}})")
      }
      count
    }
  }

  def deleteByIdAndTimestamp(id: Long, timestamp: Option[DateTime])(implicit s: DBSession) = {
    deleteBy(byIdAndTimestamp(id, timestamp))
  }

  def deleteByIdAndTimestamp(id: Long, timestamp: DateTime)(implicit s: DBSession) = {
    deleteBy(byIdAndTimestamp(id, Option(timestamp)))
  }

  override def deleteBy(where: SQLSyntax)(implicit s: DBSession): Int = {
    val count = super.deleteBy(where)(s)
    if (count == 0) {
      throw new OptimisticLockException(
        s"Conflict ${lockTimestampName} is detected (condition: '${where.value}', ${where.parameters.mkString(",")}})")
    } else {
      count
    }
  }

  override def updateById(id: Long): UpdateOperationBuilder = {
    logger.info("#updateById ignore optimistic lock. If you need to lock with version in this case, use #updateBy instead.")
    super.updateBy(byId(id))
  }

  override def deleteById(id: Long)(implicit s: DBSession): Int = {
    logger.info("#deleteById ignore optimistic lock. If you need to lock with version in this case, use #deleteBy instead.")
    super.deleteBy(byId(id))
  }

}
