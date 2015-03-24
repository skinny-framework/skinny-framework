package skinny.orm

import skinny.exception.IllegalAssociationException
import skinny.orm.feature._
import skinny.orm.feature.associations.HasOneAssociation

/**
 * Basic mapper for tables that don't have single primary key.
 *
 * @tparam Entity entity
 */
trait SkinnyNoIdMapper[Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with NoIdFinderFeature[Entity]
    with NoIdQueryingFeature[Entity]
    with NoIdAssociationsFeature[Entity]
    with StrongParametersFeature
    with AssociationsFeature[Entity] {

  override def hasOne[A](
    right: AssociationsWithIdFeature[_, A],
    merge: (Entity, Option[A]) => Entity): HasOneAssociation[Entity] = {

    throw new IllegalAssociationException(
      s"SkinnyNoIdMapper doesn't support `hasOne` relationship through single primary key (e.g. id).")
  }

}