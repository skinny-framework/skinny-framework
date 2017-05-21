package skinny.controller.feature

import skinny.micro.SkinnyMicroBase

/**
  * CORS(Cross-Origin Resource Sharing) support.
  *
  * http://www.w3.org/TR/cors/
  * http://enable-cors.org/
  */
trait CORSFeature { self: SkinnyMicroBase with BeforeAfterActionFeature =>

  beforeAction() {
    response(context).setHeader("Access-Control-Allow-Origin", "*")
  }

}
