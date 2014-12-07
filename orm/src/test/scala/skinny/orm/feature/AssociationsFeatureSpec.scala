package skinny.orm.feature

import org.scalatest._
import skinny.orm.exception.AssociationSettingsException

class AssociationsFeatureSpec extends FlatSpec with Matchers {
  behavior of "AssociationsFeature"

  it should "have #defaultIncludesMerge" in {
    intercept[AssociationSettingsException] {
      AssociationsFeature.defaultIncludesMerge(Nil, Nil)
    }
  }
}
