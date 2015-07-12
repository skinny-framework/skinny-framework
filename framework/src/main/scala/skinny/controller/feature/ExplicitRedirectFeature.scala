package skinny.controller.feature

import skinny.SkinnyEnv
import skinny.engine.SkinnyScalatraBase
import skinny.engine.response.{ SeeOther, Found, MovedPermanently, ActionResult }

/**
 * Explicit redirect method support.
 */
trait ExplicitRedirectFeature extends SkinnyScalatraBase {

  /**
   * Responds as "301 Moved Permanently"
   *
   * @return ActionResult
   */
  def redirect301(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = {
    val result = MovedPermanently(fullUrl(location, includeServletPath = false), headers, reason)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

  /**
   * Responds as "302 Found"
   *
   * @return ActionResult
   */
  def redirect302(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = {
    val result = Found(fullUrl(location, includeServletPath = false), headers, reason)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

  /**
   * Responds as "303 See Other"
   *
   * @return ActionResult
   */
  def redirect303(location: String, headers: Map[String, String] = Map.empty, reason: String = ""): ActionResult = {
    val result = SeeOther(fullUrl(location, includeServletPath = false), headers, reason)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

}
