package skinny.orm

import skinny.orm.feature._

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Entity entity
 */
trait SkinnyCRUDMapper[Entity]
    extends SkinnyMapper[Entity]
    with CRUDFeatureWithId[Long, Entity] {
  override def rawValueToId(value: Any) = value.toString.toLong
  override def idToRawValue(id: Long) = id
}

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Id id
 * @tparam Entity entity
 */
trait SkinnyCRUDMapperWithId[Id, Entity]
  extends SkinnyMapperWithId[Id, Entity]
  with CRUDFeatureWithId[Id, Entity]