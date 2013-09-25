package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime

trait SoftDeleteWithTimestampFeature[Entity] extends CRUDFeature[Entity] {

  val deletedAtFieldName = "deletedAt"

  override def defaultScopeWithoutAlias: Option[SQLSyntax] = {
    val scope = sqls.isNull(defaultAlias.support.column.field(deletedAtFieldName))
    super.defaultScopeWithoutAlias.map(_.and.append(scope)) orElse Some(scope)
  }

  override def defaultScopeWithDefaultAlias: Option[SQLSyntax] = {
    val scope = sqls.isNull(this.defaultAlias.field(deletedAtFieldName))
    super.defaultScopeWithDefaultAlias.map(_.and.append(scope)) orElse Some(scope)
  }

  override def deleteById(id: Long)(implicit s: DBSession) {
    updateById(id).withNamedValues(column.field(deletedAtFieldName) -> DateTime.now)
  }
}
