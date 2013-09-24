package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._

trait SoftDeleteWithBooleanFeature[Entity] extends CRUDFeature[Entity] {

  val isDeletedFieldName = "isDeleted"

  override def prepareDefaultScopeWithoutAlias(): Unit = {
    super.prepareDefaultScopeWithoutAlias()
    appendToDefaultScope(sqls.eq(defaultAlias.support.column.field(isDeletedFieldName), false))
  }

  override def prepareDefaultScopeWithDefaultAlias(): Unit = {
    super.prepareDefaultScopeWithDefaultAlias()
    appendToDefaultScopeWithDefaultAlias(sqls.eq(defaultAlias.field(isDeletedFieldName), false))
  }

  override def deleteById(id: Long)(implicit s: DBSession) {
    updateById(id).withNamedValues(column.field(isDeletedFieldName) -> true)
  }
}
