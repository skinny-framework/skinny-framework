package skinny.controller.feature

import skinny.micro.SkinnyMicroBase

/**
 * Supports snake_case'd keys for parameter names.
 */
trait SnakeCasedParamKeysFeature extends SkinnyMicroBase {

  /**
   * Defines use snake_case'd keys.
   *
   * @return true if use snake_case keys (default: false)
   */
  protected def useSnakeCasedParamKeys: Boolean = false

}
