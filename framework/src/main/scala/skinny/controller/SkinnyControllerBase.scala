package skinny.controller

import skinny.controller.feature._
import skinny.micro.ThreadLocalFeatures

trait SkinnyControllerBase
    extends SkinnyControllerCommonBase
    with ThreadLocalFeatures
    with BeforeAfterActionFeature
    with ThreadLocalRequestFeature {

}
