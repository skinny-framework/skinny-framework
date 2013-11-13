package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import skinny.orm.feature.associations._
import scalikejdbc._, SQLInterpolation._
import skinny.orm.feature.includes.IncludesQueryRepository
import skinny.util.JavaReflectAPI

/**
 * Provides #includes APIs.
 */
trait IncludesFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with AssociationsFeature[Entity] {

  private[skinny] val includedBelongsToAssociations: Seq[BelongsToAssociation[Entity]] = Nil
  private[skinny] val includedHasOneAssociations: Seq[HasOneAssociation[Entity]] = Nil
  private[skinny] val includedHasManyAssociations: Seq[HasManyAssociation[Entity]] = Nil

  /**
   * Adds includes conditions.
   *
   * @param associations associations
   * @return extended self
   */
  def includes(associations: Association[_]*): IncludesFeature[Entity] with FinderFeature[Entity] with QueryingFeature[Entity] = {
    val _self = this
    val _associations = associations
    val _belongsTo = associations.filter(_.isInstanceOf[BelongsToAssociation[Entity]]).map(_.asInstanceOf[BelongsToAssociation[Entity]])
    val _hasOne = associations.filter(_.isInstanceOf[HasOneAssociation[Entity]]).map(_.asInstanceOf[HasOneAssociation[Entity]])
    val _hasMany = associations.filter(_.isInstanceOf[HasManyAssociation[Entity]]).map(_.asInstanceOf[HasManyAssociation[Entity]])

    new IncludesFeature[Entity] with FinderFeature[Entity] with QueryingFeature[Entity] {
      override protected val underlying = _self
      override private[skinny] val includedBelongsToAssociations = _self.includedBelongsToAssociations ++ _belongsTo
      override private[skinny] val includedHasOneAssociations = _self.includedHasOneAssociations ++ _hasOne
      override private[skinny] val includedHasManyAssociations = _self.includedHasManyAssociations ++ _hasMany

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

  /**
   * Returns ids from entities.
   *
   * @param entities entities
   * @param primaryKeyName primary key name
   * @return ids
   */
  private[this] def toIds(entities: Seq[Any], primaryKeyName: String): Seq[Long] = {
    entities.flatMap { e =>
      JavaReflectAPI.getter(e, primaryKeyName) match {
        case Some(v: Long) => Some(v)
        case Some(v) => Some(v.toString.toLong)
        case None => None
        case null => None
        case v => Option(v.toString.toLong)
      }
    }
  }

  /**
   * Applies includes operations to query results.
   *
   * @param entities entities
   * @param s session
   * @param repository repository
   * @return entities with included attributes
   */
  def withIncludedAttributes(entities: List[Entity])(
    implicit s: DBSession, repository: IncludesQueryRepository[Entity]): List[Entity] = {
    try {
      val withBelongsTo = includedBelongsToAssociations.foldLeft(entities) {
        case (entities, assoc) =>
          val ids = toIds(repository.entitiesFor(assoc.extractor), assoc.mapper.primaryKeyName)
          assoc.extractor.includesMerge(entities,
            assoc.extractor.mapper.asInstanceOf[FinderFeature[Entity]].findAllByIds(ids: _*)).toList
      }
      val withHasOne = includedHasOneAssociations.foldLeft(withBelongsTo) {
        case (entities, assoc) =>
          val ids = toIds(repository.entitiesFor(assoc.extractor), assoc.mapper.primaryKeyName)
          assoc.extractor.includesMerge(entities,
            assoc.extractor.mapper.asInstanceOf[FinderFeature[Entity]].findAllByIds(ids: _*)).toList
      }
      includedHasManyAssociations.foldLeft(withHasOne) {
        case (entities, assoc) =>
          val ids = toIds(repository.entitiesFor(assoc.extractor), assoc.mapper.primaryKeyName)
          assoc.extractor.includesMerge(entities,
            assoc.extractor.mapper.asInstanceOf[FinderFeature[Entity]].findAllByIds(ids: _*)).toList
      }

    } catch {
      case e: ClassCastException =>
        throw new IllegalStateException(s"Failed to execute includes query because ${e.getMessage}!")
    }
  }

  /**
   * Applies includes operations to query result.
   *
   * @param entity entity
   * @param s session
   * @param repository repository
   * @return entity with included attributes
   */
  def withIncludedAttributes(entity: Option[Entity])(
    implicit s: DBSession, repository: IncludesQueryRepository[Entity]): Option[Entity] = {
    withIncludedAttributes(entity.toList).headOption
  }

}
