package skinny.json

/**
 * JSON String operations for Angular application's server side.
 *
 * - camelCase keys
 * - JSON vulnerability protection enabled by default
 */
trait AngularJSONStringOps extends JSONStringOps with JSONStringOpsConfig {

  // JSON vulnerability protection enabled by default
  override protected def useJSONVulnerabilityProtection: Boolean = true

  // camelCase keys by default
  override protected def useUnderscoreKeysForJSON: Boolean = false

}

object AngularJSONStringOps extends AngularJSONStringOps
