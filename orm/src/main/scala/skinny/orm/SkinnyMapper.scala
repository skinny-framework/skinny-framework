package skinny.orm

import skinny.orm.feature._

trait SkinnyMapper[Entity]
  extends BasicFeature[Entity]
  with ConnectionPoolFeature
  with AutoSessionFeature
  with AssociationsFeature[Entity]
  with StrongParametersFeature
