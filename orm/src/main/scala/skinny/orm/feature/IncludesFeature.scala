package skinny.orm.feature

import skinny.orm.SkinnyMapperBase
import skinny.orm.feature.associations._
import scalikejdbc._, SQLInterpolation._

/**
 * Provides #includes APIs.
 */
trait IncludesFeature[Entity]
    extends SkinnyMapperBase[Entity]
    with AssociationsFeature[Entity] {

  private[skinny] val includedBelongsToAssociations: Seq[BelongsToAssociation[Entity]] = Nil
  private[skinny] val includedHasOneAssociations: Seq[HasOneAssociation[Entity]] = Nil
  private[skinny] val includedHasManyAssociations: Seq[HasManyAssociation[Entity]] = Nil

  /*
   * TODO includes feature design document
   *
   * - should support only single nested attributes (A -> B -> C -> D)'s D won't be supported
   * - should work with JoinsFeature perfectly
   * - should execute in-clause query with all primary keys for each associations
   * - should extract included attributes for each record map each record using extractor?
   * - should have more debug logging for mixin behavior (for also other features)
   */
  def includes(associations: Association[_]*) = this

}
