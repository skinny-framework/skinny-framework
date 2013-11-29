package skinny.orm

import skinny.orm.feature._

/**
 * Basic SkinnyMapper implementation.
 *
 * @tparam Entity entity
 */
trait SkinnyMapper[Entity]
  extends SkinnyMapperBase[Entity]
  with ConnectionPoolFeature
  with AutoSessionFeature
  with DynamicTableNameFeature[Entity]
  with AssociationsFeature[Entity]
  with StrongParametersFeature
