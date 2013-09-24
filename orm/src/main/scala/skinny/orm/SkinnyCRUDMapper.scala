package skinny.orm

import skinny.orm.feature._

trait SkinnyCRUDMapper[Entity]
  extends SkinnyMapper[Entity]
  with CRUDFeature[Entity]
