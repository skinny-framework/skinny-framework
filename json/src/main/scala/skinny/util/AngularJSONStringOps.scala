package skinny.util

/**
 * JSON String operations for Angular application's server side.
 *
 * - camelCase keys
 * - JSON vulnerability protection enabled by default
 */
trait AngularJSONStringOps extends JSONStringOps {

  // JSON vulnerability protection enabled by default
  override protected def useJSONVulnerabilityProtection = true

  // camelCase keys by default
  override protected def useUnderscoreKeysForJSON = false

}

object AngularJSONStringOps extends AngularJSONStringOps
