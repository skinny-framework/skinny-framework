package skinny.orm

import skinny.orm.feature._

/**
 * Out-of-the-box Skinny-ORM CRUD mapper.
 *
 * @tparam Entity entity
 */
trait SkinnyCRUDMapper[Entity]
  extends SkinnyMapper[Entity]
  with CRUDFeature[Entity]
