package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

trait SoftDeleteWithTimestampFeature[Entity] extends CRUDFeature[Entity] {

  val deletedAtFieldName = "deletedAt"

  override def prepareDefaultScopeWithoutAlias(): Unit = {
    super.prepareDefaultScopeWithoutAlias()
    appendToDefaultScope(sqls.isNull(defaultAlias.support.column.field(deletedAtFieldName)))
  }

  override def prepareDefaultScopeWithDefaultAlias(): Unit = {
    super.prepareDefaultScopeWithDefaultAlias()
    appendToDefaultScopeWithDefaultAlias(sqls.isNull(this.defaultAlias.field(deletedAtFieldName)))
  }

  override def deleteById(id: Long)(implicit s: DBSession) {
    updateById(id).withNamedValues(column.field(deletedAtFieldName) -> DateTime.now)
  }
}
