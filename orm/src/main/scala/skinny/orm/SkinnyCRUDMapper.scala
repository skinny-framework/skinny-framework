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
