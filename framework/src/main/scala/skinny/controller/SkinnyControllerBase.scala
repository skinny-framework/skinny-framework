package skinny.controller

import skinny.controller.feature._
import skinny.engine.ThreadLocalFeatures

trait SkinnyControllerBase
    extends SkinnyControllerCommonBase
    with ThreadLocalFeatures {

}
