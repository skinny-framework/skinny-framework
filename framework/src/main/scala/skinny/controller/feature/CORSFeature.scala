package skinny.controller.feature

import skinny.engine.SkinnyScalatraBase

/**
 * CORS(Cross-Origin Resource Sharing) support.
 *
 * http://www.w3.org/TR/cors/
 * http://enable-cors.org/
 */
trait CORSFeature { self: SkinnyScalatraBase with BeforeAfterActionFeature =>

  beforeAction() {
    response.setHeader("Access-Control-Allow-Origin", "*")
  }

}
