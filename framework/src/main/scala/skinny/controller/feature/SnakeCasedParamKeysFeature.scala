package skinny.controller.feature

import skinny.engine.SkinnyEngineBase

/**
 * Supports snake_case'd keys for parameter names.
 */
trait SnakeCasedParamKeysFeature extends SkinnyEngineBase {

  /**
   * Defines use snake_case'd keys.
   *
   * @return true if use snake_case keys (default: false)
   */
  protected def useSnakeCasedParamKeys: Boolean = false

}
