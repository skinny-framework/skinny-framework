package skinny.orm

import skinny.orm.feature._

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