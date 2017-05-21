package skinny.orm

import skinny.orm.feature._

/**
  * Basic SkinnyMapper implementation.
  *
  * @tparam Id id
  * @tparam Entity entity
  */
trait SkinnyMapperWithId[Id, Entity]
    extends SkinnyMapperBase[Entity]
    with ConnectionPoolFeature
    with AutoSessionFeature
    with IdFeature[Id]
    with AssociationsWithIdFeature[Id, Entity]
    with FinderFeatureWithId[Id, Entity]
    with QueryingFeatureWithId[Id, Entity]
    with DynamicTableNameFeatureWithId[Id, Entity]
    with StrongParametersFeature
