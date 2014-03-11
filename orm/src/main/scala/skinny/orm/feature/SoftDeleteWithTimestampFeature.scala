package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import skinny.orm.Alias

/**
 * Soft delete with timestamp value.
 *
 * @tparam Entity entity
 */
trait SoftDeleteWithTimestampFeature[Entity]
  extends SoftDeleteWithTimestampFeatureWithId[Long, Entity]

trait SoftDeleteWithTimestampFeatureWithId[Id, Entity] extends CRUDFeatureWithId[Id, Entity] {

  /**
   * deleted_at timestamp field name.
   */
  val deletedAtFieldName = "deletedAt"

  override def defaultScopeForUpdateOperations: Option[SQLSyntax] = {
    val c = defaultAlias.support.column
    val scope = sqls.isNull(c.field(deletedAtFieldName))
    super.defaultScopeForUpdateOperations.map(_.and.append(scope)) orElse Some(scope)
  }

  override def defaultScope(alias: Alias[Entity]): Option[SQLSyntax] = {
    val scope = sqls.isNull(alias.field(deletedAtFieldName))
    super.defaultScope(alias).map(_.and.append(scope)) orElse Some(scope)
  }

  override def deleteBy(where: SQLSyntax)(implicit s: DBSession = autoSession): Int = {
    updateBy(where).withNamedValues(column.field(deletedAtFieldName) -> DateTime.now)
  }
}
