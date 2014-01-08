package skinny.orm

import skinny.orm.feature._

/**
 * Basic SkinnyMapper implementation.
 *
 * @tparam Entity entity
 */
trait SkinnyMapper[Entity]
    extends SkinnyMapperWithId[Long, Entity] {

  override def generateId = ???
  override def rawValueToId(rawValue: Any) = rawValue.toString.toLong
  override def idToRawValue(id: Long) = id
}

trait SkinnyMapperWithId[Id, Entity]
  extends SkinnyMapperBase[Entity]
  with ConnectionPoolFeature
  with AutoSessionFeature
  with AssociationsFeature[Entity]
  with IdFeature[Id]
  with DynamicTableNameFeatureWithId[Id, Entity]
  with StrongParametersFeature
