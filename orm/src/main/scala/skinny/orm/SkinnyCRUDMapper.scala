package skinny.orm

import skinny.orm.feature._

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Entity entity
 */
trait SkinnyCRUDMapper[Entity] extends SkinnyMapper[Entity]
  with CRUDFeatureWithId[Long, Entity]

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Id id
 * @tparam Entity entity
 */
trait SkinnyCRUDMapperWithId[Id, Entity]
    extends SkinnyMapperWithId[Id, Entity]
    with CRUDFeatureWithId[Id, Entity] {

  override def useAutoIncrementPrimaryKey = false
  override def useExternalIdGenerator = true
}
