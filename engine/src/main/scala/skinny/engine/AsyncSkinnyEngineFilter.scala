package skinny.engine

import javax.servlet.Filter

trait AsyncSkinnyEngineFilter
    extends Filter
    with SkinnyEngineFilterBase
    with AsyncFeatures {

}
