package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.orm.PermittedStrongParameters
import org.joda.time.DateTime

trait TimestampsFeature[Entity] extends CRUDFeature[Entity] {

  val createdAtFieldName = "createdAt"
  val updatedAtFieldName = "updatedAt"

  override protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    if (!strongParameters.params.contains(createdAtFieldName)) {
      val createdAt: (SQLSyntax, Any) = defaultAlias.support.column.field(createdAtFieldName) -> DateTime.now
      super.namedValuesForCreation(strongParameters) :+ createdAt
    } else {
      super.namedValuesForCreation(strongParameters)
    }
  }

  override def createWithNamedValues(namedValues: (SQLInterpolation.SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Long = {
    val createdAt = defaultAlias.support.column.field(createdAtFieldName)
    val namedValuesWithCreatedAt = {
      if (namedValues.exists(_._1.value == createdAt.value)) namedValues
      else namedValues :+ (createdAt -> DateTime.now)
    }
    super.createWithNamedValues(namedValuesWithCreatedAt: _*)
  }

  override def updateById(id: Long) = new UpdateOperationBuilderWithUpdateAt(this, id)

  class UpdateOperationBuilderWithUpdateAt(self: CRUDFeature[Entity], id: Long) extends UpdateOperationBuilder(self, byId(id)) {
    val column = defaultAlias.support.column
    addAttributeToBeUpdated(column.field(updatedAtFieldName) -> DateTime.now)
  }

}
