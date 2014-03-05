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
    val (params, column, now) = (strongParameters.params, defaultAlias.support.column, DateTime.now)
    val additionalValues: Seq[(SQLSyntax, Any)] = {
      val values = new collection.mutable.ListBuffer[(SQLSyntax, Any)]
      if (!params.contains(createdAtFieldName)) values.append(column.field(createdAtFieldName) -> now)
      if (!params.contains(updatedAtFieldName)) values.append(column.field(updatedAtFieldName) -> now)
      values.toSeq
    }
    super.namedValuesForCreation(strongParameters) ++ additionalValues
  }

  override def createWithNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Id = {
    val (column, now) = (defaultAlias.support.column, DateTime.now)
    val additionalValues: Seq[(SQLSyntax, Any)] = {
      val values = new collection.mutable.ListBuffer[(SQLSyntax, Any)]
      if (!namedValues.exists(_._1 == column.field(createdAtFieldName))) values.append(column.field(createdAtFieldName) -> now)
      if (!namedValues.exists(_._1 == column.field(updatedAtFieldName))) values.append(column.field(updatedAtFieldName) -> now)
      values.toSeq
    }
    super.createWithNamedValues((namedValues ++ additionalValues): _*)
  }

  override def updateBy(where: SQLSyntax): UpdateOperationBuilder = {
    val builder = super.updateBy(where)
    builder.addAttributeToBeUpdated(column.field(updatedAtFieldName) -> DateTime.now)
    builder
  }

}
