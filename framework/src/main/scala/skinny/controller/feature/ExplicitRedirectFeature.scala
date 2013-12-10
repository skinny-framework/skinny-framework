package skinny.controller.feature

import org.scalatra._

/**
 * Explicit redirect method support.
 */
trait ExplicitRedirectFeature extends ScalatraBase {

  /**
   * Responds as "301 Moved Permanently"
   *
   * @return ActionResult
   */
  def redirect301(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = MovedPermanently(location, headers, reason)

  /**
   * Responds as "302 Found"
   *
   * @return ActionResult
   */
  def redirect302(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = Found(location, headers, reason)

  /**
   * Responds as "303 See Other"
   *
   * @return ActionResult
   */
  def redirect303(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = SeeOther(location, headers, reason)

}
