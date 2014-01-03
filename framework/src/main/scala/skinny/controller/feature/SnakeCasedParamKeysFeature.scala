package skinny.controller.feature

/**
 * Supports snake_case'd keys for parameter names.
 */
trait SnakeCasedParamKeysFeature extends org.scalatra.ScalatraBase {

  /**
   * Defines use snake_case'd keys.
   *
   * @return true if use snake_case keys (default: false)
   */
  protected def useSnakeCaseKeys: Boolean = false

}
