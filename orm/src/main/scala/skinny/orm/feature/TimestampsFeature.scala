package skinny.orm.feature

import scalikejdbc._, SQLInterpolation._
import skinny.PermittedStrongParameters
import org.joda.time.DateTime

/**
 * ActiveRecord timestamps feature.
 *
 * @tparam Entity entity
 */
trait TimestampsFeature[Entity]
  extends TimestampsFeatureWithId[Long, Entity]

trait TimestampsFeatureWithId[Id, Entity] extends CRUDFeatureWithId[Id, Entity] {

  /**
   * createdAt field name.
   */
  val createdAtFieldName = "createdAt"

  /**
   * updatedAt field name.
   */
  val updatedAtFieldName = "updatedAt"

  override protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    if (!strongParameters.params.contains(createdAtFieldName)) {
      val createdAt: (SQLSyntax, Any) = defaultAlias.support.column.field(createdAtFieldName) -> DateTime.now
      super.namedValuesForCreation(strongParameters) :+ createdAt
    } else {
      super.namedValuesForCreation(strongParameters)
    }
  }

  override def createWithNamedValues(namedValues: (SQLInterpolation.SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Id = {
    val createdAt = defaultAlias.support.column.field(createdAtFieldName)
    val namedValuesWithCreatedAt = {
      if (namedValues.exists(_._1 == createdAt)) namedValues
      else namedValues :+ (createdAt -> DateTime.now)
    }
    super.createWithNamedValues(namedValuesWithCreatedAt: _*)
  }

  override def updateById(id: Id): UpdateOperationBuilder = new UpdateOperationBuilderWithUpdateAt(this, id)

  class UpdateOperationBuilderWithUpdateAt(self: CRUDFeatureWithId[Id, Entity], id: Id)
      extends UpdateOperationBuilder(self, byId(id), beforeUpdateByHandlers, afterUpdateByHandlers) {
    val column = defaultAlias.support.column
    addAttributeToBeUpdated(column.field(updatedAtFieldName) -> DateTime.now)
  }

}
