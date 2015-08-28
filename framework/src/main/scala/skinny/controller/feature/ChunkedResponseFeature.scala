package skinny.controller.feature

import skinny.micro.SkinnyMicroBase
import skinny.micro.contrib.ChunkedResponseSupport

/**
 * Chunked Response (Transfer-Encoding: chunked).
 */
trait ChunkedResponseFeature extends SkinnyMicroBase with ChunkedResponseSupport {

}