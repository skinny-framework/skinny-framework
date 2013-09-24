package skinny.orm.feature.associations

import skinny.orm._
import scala.collection.mutable
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature._

class CRUDFeatureWithAssociations[Entity](
    val underlying: CRUDFeature[Entity],
    belongsTo: Seq[BelongsToAssociation[Entity]],
    hasOne: Seq[HasOneAssociation[Entity]],
    hasMany: Seq[HasManyAssociation[Entity]]) extends CRUDFeature[Entity] {

  override def defaultAlias = underlying.defaultAlias

  override def createAlias(name: String): Alias[Entity] = underlying.createAlias(name)

  override protected def toDefaultForeignKeyName[A](mapper: AssociationsFeature[A]): String = {
    if (mapper.isInstanceOf[CRUDFeatureWithAssociations[Entity]]) {
      val withAssociations = mapper.asInstanceOf[CRUDFeatureWithAssociations[Entity]]
      toDefaultForeignKeyName(withAssociations.underlying)
    } else {
      super.toDefaultForeignKeyName(underlying)
    }
  }

  override val defaultJoinDefinitions: mutable.LinkedHashSet[JoinDefinition[_]] = underlying.defaultJoinDefinitions
  override val defaultBelongsToExtractors: mutable.LinkedHashSet[BelongsToExtractor[Entity]] = underlying.defaultBelongsToExtractors
  override val defaultHasOneExtractors: mutable.LinkedHashSet[HasOneExtractor[Entity]] = underlying.defaultHasOneExtractors
  override val defaultOneToManyExtractors: mutable.LinkedHashSet[ToManyExtractor[Entity]] = underlying.defaultOneToManyExtractors

  override def extract(rs: WrappedResultSet, n: ResultName[Entity]) = underlying.extract(rs, n)

  override def selectQuery: SelectSQLBuilder[Entity] = {
    selectQueryWithAssociations(
      underlying.selectQuery,
      belongsTo.toSet,
      hasOne.toSet,
      hasMany.toSet)
  }

  override def withExtractor(sql: SQL[Entity, NoExtractor]): SQL[Entity, HasExtractor] = {
    withExtractor(
      sql,
      belongsTo.toSet,
      hasOne.toSet,
      hasMany.toSet)
  }

  override def joins(associations: Association[_]*): CRUDFeatureWithAssociations[Entity] = {
    val newBelongsTo = belongsTo ++ associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val newHasOne = hasOne ++ associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val newHasMany = hasMany ++ associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])
    new CRUDFeatureWithAssociations[Entity](
      underlying,
      newBelongsTo,
      newHasOne,
      newHasMany)
  }
}
