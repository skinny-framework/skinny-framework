package skinny.controller.feature

import skinny.SkinnyEnv
import skinny.micro.SkinnyMicroBase
import skinny.micro.context.SkinnyContext
import skinny.micro.response.{ SeeOther, Found, MovedPermanently, ActionResult }

/**
 * Explicit redirect method support.
 */
trait ExplicitRedirectFeature extends SkinnyMicroBase {

  /**
   * Responds as "301 Moved Permanently"
   *
   * @return ActionResult
   */
  def redirect301(location: String, headers: Map[String, String] = Map.empty)(
    implicit ctx: SkinnyContext = context): ActionResult = {
    val result = MovedPermanently(fullUrl(location, includeServletPath = false), headers)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

  /**
   * Responds as "302 Found"
   *
   * @return ActionResult
   */
  def redirect302(location: String, headers: Map[String, String] = Map.empty)(
    implicit ctx: SkinnyContext = context): ActionResult = {
    val result = Found(fullUrl(location, includeServletPath = false), headers)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

  /**
   * Responds as "303 See Other"
   *
   * @return ActionResult
   */
  def redirect303(location: String, headers: Map[String, String] = Map.empty)(
    implicit ctx: SkinnyContext = context): ActionResult = {
    val result = SeeOther(fullUrl(location, includeServletPath = false), headers)
    if (SkinnyEnv.isTest()) result else halt(result)
  }

}
