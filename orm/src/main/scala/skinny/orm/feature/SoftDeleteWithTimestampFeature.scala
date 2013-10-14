package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

/**
 * Soft delete with timestamp value.
 *
 * @tparam Entity entity
 */
trait SoftDeleteWithTimestampFeature[Entity] extends CRUDFeature[Entity] {

  /**
   * deleted_at timestamp field name.
   */
  val deletedAtFieldName = "deletedAt"

  override def defaultScopeForUpdateOperations: Option[SQLSyntax] = {
    val scope = sqls.isNull(defaultAlias.support.column.field(deletedAtFieldName))
    super.defaultScopeForUpdateOperations.map(_.and.append(scope)) orElse Some(scope)
  }

  override def defaultScopeWithDefaultAlias: Option[SQLSyntax] = {
    val scope = sqls.isNull(this.defaultAlias.field(deletedAtFieldName))
    super.defaultScopeWithDefaultAlias.map(_.and.append(scope)) orElse Some(scope)
  }

  override def deleteBy(where: SQLSyntax)(implicit s: DBSession): Int = {
    updateBy(where).withNamedValues(column.field(deletedAtFieldName) -> DateTime.now)
  }
}
