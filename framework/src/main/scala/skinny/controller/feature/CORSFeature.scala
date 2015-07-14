package skinny.controller.feature

import skinny.engine.SkinnyEngineBase

/**
 * CORS(Cross-Origin Resource Sharing) support.
 *
 * http://www.w3.org/TR/cors/
 * http://enable-cors.org/
 */
trait CORSFeature { self: SkinnyEngineBase with BeforeAfterActionFeature =>

  beforeAction() {
    response.setHeader("Access-Control-Allow-Origin", "*")
  }

}
