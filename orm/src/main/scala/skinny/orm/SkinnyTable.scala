package skinny.orm

import skinny.orm.feature._

/**
 * Table definition without single primary key specified by default.
 *
 * @tparam Entity entity
 */
trait SkinnyTable[Entity] extends SkinnyMapperBase[Entity]
  with ConnectionPoolFeature
  with AutoSessionFeature
  with AssociationsFeature[Entity]
  with QueryingFeature[Entity]
  with StrongParametersFeature
