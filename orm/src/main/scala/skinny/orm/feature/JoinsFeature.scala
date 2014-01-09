package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import skinny.orm.feature.associations.{ HasManyAssociation, HasOneAssociation, BelongsToAssociation, Association }
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository

/**
 * Provides #joins APIs.
 */
trait JoinsFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with AssociationsFeature[Entity] { self: IdFeature[_] =>

  private[skinny] val belongsToAssociations: Seq[BelongsToAssociation[Entity]] = Nil
  private[skinny] val hasOneAssociations: Seq[HasOneAssociation[Entity]] = Nil
  private[skinny] val hasManyAssociations: Seq[HasManyAssociation[Entity]] = Nil

  /**
   * Appends join definition on runtime.
   *
   * @param associations associations
   * @return self
   */
  def joins[Id](associations: Association[_]*): JoinsFeature[Entity] with IdFeature[Id] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] = {
    val _self = this
    val _associations = associations
    val _belongsTo = associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val _hasOne = associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val _hasMany = associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])

    new JoinsFeature[Entity] with IdFeature[Id] with FinderFeatureWithId[Id, Entity] with QueryingFeatureWithId[Id, Entity] {
      override protected val underlying = _self
      override def defaultAlias = _self.defaultAlias

      override def rawValueToId(value: Any) = _self.rawValueToId(value).asInstanceOf[Id]
      override def idToRawValue(id: Id) = id

      override private[skinny] val belongsToAssociations = _self.belongsToAssociations ++ _belongsTo
      override private[skinny] val hasOneAssociations = _self.hasOneAssociations ++ _hasOne
      override private[skinny] val hasManyAssociations = _self.hasManyAssociations ++ _hasMany

      override val associations = _self.associations ++ _associations
      override val defaultJoinDefinitions = _self.defaultJoinDefinitions
      override val defaultBelongsToExtractors = _self.defaultBelongsToExtractors
      override val defaultHasOneExtractors = _self.defaultHasOneExtractors
      override val defaultOneToManyExtractors = _self.defaultOneToManyExtractors

      override def autoSession = underlying.autoSession
      override def connectionPoolName = underlying.connectionPoolName
      override def connectionPool = underlying.connectionPool

      def extract(rs: WrappedResultSet, n: SQLInterpolation.ResultName[Entity]) = underlying.extract(rs, n)
    }
  }

  def selectQueryWithAssociations: SelectSQLBuilder[Entity] = {
    selectQueryWithAdditionalAssociations(
      defaultSelectQuery,
      belongsToAssociations,
      hasOneAssociations,
      hasManyAssociations)
  }

  override def extract(sql: SQL[Entity, NoExtractor])(
    implicit includesRepository: IncludesQueryRepository[Entity]): SQL[Entity, HasExtractor] = {
    super.extractWithAssociations(
      sql,
      belongsToAssociations,
      hasOneAssociations,
      hasManyAssociations)
  }
}
