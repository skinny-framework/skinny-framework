package skinny.orm

import skinny.orm.feature._

/**
  * CRUD mapper for tables that don't have single primary key.
  *
  * @tparam Entity entity
  */
trait SkinnyNoIdCRUDMapper[Entity] extends SkinnyNoIdMapper[Entity] with NoIdCUDFeature[Entity]
