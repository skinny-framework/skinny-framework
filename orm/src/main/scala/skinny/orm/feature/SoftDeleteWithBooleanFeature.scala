package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._

/**
 * Soft delete with boolean value.
 *
 * @tparam Entity entity
 */
trait SoftDeleteWithBooleanFeature[Entity] extends CRUDFeature[Entity] {

  /**
   * is deleted flag field name.
   */
  val isDeletedFieldName = "isDeleted"

  override def defaultScopeForUpdateOperations: Option[SQLSyntax] = {
    val scope = sqls.eq(defaultAlias.support.column.field(isDeletedFieldName), false)
    super.defaultScopeForUpdateOperations.map(_.and.append(scope)) orElse Some(scope)
  }

  override def defaultScopeWithDefaultAlias: Option[SQLSyntax] = {
    val scope = sqls.eq(defaultAlias.field(isDeletedFieldName), false)
    super.defaultScopeWithDefaultAlias.map(_.and.append(scope)) orElse Some(scope)
  }

  override def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Int = {
    updateBy(where).withNamedValues(column.field(isDeletedFieldName) -> true)
  }
}
