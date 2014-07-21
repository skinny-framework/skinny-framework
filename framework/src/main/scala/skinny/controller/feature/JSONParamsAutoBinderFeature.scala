package skinny.controller.feature

import org.scalatra.json.JsonSupport
import org.scalatra.{ Params, ScalatraParams, SkinnyScalatraBase }
import skinny.logging.Logging
import javax.servlet.http.HttpServletRequest

/**
 * Merging JSON request body into Scalatra params.
 *
 * When you'd like to avoid merging JSON request body into params in some actions, please separate controllers.
 */
trait JSONParamsAutoBinderFeature
    extends SkinnyScalatraBase with JSONFeature with Logging {

  /**
   * Merge parsedBody (JValue) into params if possible.
   */
  override def params(implicit request: HttpServletRequest): Params = {
    if (request.get(JsonSupport.ParsedBodyKey).isDefined) {
      try {
        val jsonParams: Map[String, Seq[String]] = parsedBody.extract[Map[String, String]].mapValues(v => Seq(v))
        val mergedParams: Map[String, Seq[String]] = getMergedMultiParams(multiParams, jsonParams)
        new ScalatraParams(mergedParams)
      } catch {
        case e: Exception =>
          logger.debug(s"Failed to parse JSON body because ${e.getMessage}")
          super.params(request)
      }
    } else {
      super.params(request)
    }
  }

  protected def getMergedMultiParams(params1: Map[String, Seq[String]], params2: Map[String, Seq[String]]): Map[String, Seq[String]] = {
    (params1.toSeq ++ params2.toSeq).groupBy(_._1).mapValues(_.flatMap(_._2))
  }

}
