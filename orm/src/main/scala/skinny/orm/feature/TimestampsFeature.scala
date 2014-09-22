package skinny.orm.feature

import scalikejdbc._
import skinny.PermittedStrongParameters
import org.joda.time.DateTime
import skinny.orm.SkinnyMapperBase

/**
 * ActiveRecord timestamps feature.
 *
 * @tparam Entity entity
 */
trait TimestampsFeature[Entity]
  extends TimestampsFeatureWithId[Long, Entity]

trait BaseTimestampsFeature[Entity] {
  self: SkinnyMapperBase[Entity] =>

  /**
   * createdAt field name.
   */
  def createdAtFieldName = "createdAt"

  /**
   * updatedAt field name.
   */
  def updatedAtFieldName = "updatedAt"

  protected def timestampValues(exists: String => Boolean): Seq[(SQLSyntax, Any)] = {
    val (column, now) = (defaultAlias.support.column, DateTime.now)
    val builder = List.newBuilder[(SQLSyntax, Any)]
    if (!exists(createdAtFieldName)) builder += column.field(createdAtFieldName) -> now
    if (!exists(updatedAtFieldName)) builder += column.field(updatedAtFieldName) -> now
    builder.result()
  }
}

trait TimestampsFeatureWithId[Id, Entity] extends CRUDFeatureWithId[Id, Entity] with BaseTimestampsFeature[Entity] {
  override protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    val additionalValues = timestampValues(strongParameters.params.contains)
    super.namedValuesForCreation(strongParameters) ++ additionalValues
  }

  override def createWithNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Id = {
    val additionalValues = timestampValues(name => namedValues.exists(_._1 == column.field(name)))
    super.createWithNamedValues(namedValues ++ additionalValues: _*)
  }

  override def updateBy(where: SQLSyntax): UpdateOperationBuilder = {
    val builder = super.updateBy(where)
    builder.addAttributeToBeUpdated(column.field(updatedAtFieldName) -> DateTime.now)
    builder
  }
}

trait NoIdTimestampsFeature[Entity] extends NoIdCUDFeature[Entity] with BaseTimestampsFeature[Entity] {

  override protected def namedValuesForCreation(strongParameters: PermittedStrongParameters): Seq[(SQLSyntax, Any)] = {
    val additionalValues = timestampValues(strongParameters.params.contains)
    super.namedValuesForCreation(strongParameters) ++ additionalValues
  }

  override def createWithNamedValues(namedValues: (SQLSyntax, Any)*)(implicit s: DBSession = autoSession): Any = {
    val additionalValues = timestampValues(name => namedValues.exists(_._1 == column.field(name)))
    super.createWithNamedValues(namedValues ++ additionalValues: _*)
  }

  override def updateBy(where: SQLSyntax): UpdateOperationBuilder = {
    val builder = super.updateBy(where)
    builder.addAttributeToBeUpdated(column.field(updatedAtFieldName) -> DateTime.now)
    builder
  }
}

