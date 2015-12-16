package skinny.orm.feature

import scalikejdbc._
import skinny.orm._

/**
 * Soft delete with boolean value.
 *
 * @tparam Entity entity
 */
trait SoftDeleteWithBooleanFeature[Entity]
  extends SoftDeleteWithBooleanFeatureWithId[Long, Entity]

trait SoftDeleteWithBooleanFeatureWithId[Id, Entity] extends CRUDFeatureWithId[Id, Entity] {

  /**
   * is deleted flag field name.
   */
  def isDeletedFieldName: String = "isDeleted"

  override def defaultScopeForUpdateOperations: Option[SQLSyntax] = {
    val c = defaultAlias.support.column
    val scope = sqls.eq(c.field(isDeletedFieldName), false)
    super.defaultScopeForUpdateOperations.map(_.and.append(scope)) orElse Some(scope)
  }

  override def defaultScope(alias: Alias[Entity]): Option[SQLSyntax] = {
    val scope = sqls.eq(alias.field(isDeletedFieldName), false)
    super.defaultScope(alias).map(_.and.append(scope)) orElse Some(scope)
  }

  override def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Int = {
    updateBy(where).withNamedValues(column.field(isDeletedFieldName) -> true)
  }
}
