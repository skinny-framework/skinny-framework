package skinny.controller

import skinny.controller.feature.{ AsyncBeforeAfterActionFeature, SkinnyControllerCommonBase }
import skinny.micro.AsyncFeatures

trait AsyncSkinnyControllerBase
    extends SkinnyControllerCommonBase
    with AsyncFeatures
    with AsyncBeforeAfterActionFeature {
}
