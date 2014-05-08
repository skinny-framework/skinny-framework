package skinny.orm

import skinny.orm.feature._

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Id id
 * @tparam Entity entity
 */
trait SkinnyCRUDMapperWithId[Id, Entity]
  extends SkinnyMapperWithId[Id, Entity]
  with CRUDFeatureWithId[Id, Entity]