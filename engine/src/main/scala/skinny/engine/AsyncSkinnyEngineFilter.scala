package skinny.engine

import javax.servlet.Filter

/**
 * Async skinny-engine filter.
 */
trait AsyncSkinnyEngineFilter
    extends Filter
    with SkinnyEngineFilterBase
    with AsyncFeatures {

}
