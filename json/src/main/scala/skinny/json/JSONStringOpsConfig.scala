package skinny.json

import org.json4s.{ DefaultFormats, Formats }

trait JSONStringOpsConfig { self: JSONStringOps =>

  /**
   * Use the prefix for JSON Vulnerability Protection.
   * see: "https://docs.angularjs.org/api/ng/service/$http"
   */
  protected def useJSONVulnerabilityProtection: Boolean = false

  /**
   * the prefix for JSON Vulnerability Protection.
   * see: "https://docs.angularjs.org/api/ng/service/$http"
   */
  protected def prefixForJSONVulnerabilityProtection: String = ")]}',\n"

  /**
   * Default key policy.
   */
  protected def useUnderscoreKeysForJSON: Boolean = true

  /**
   * JSON format support implicitly.
   */
  protected implicit val jsonFormats: Formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

}
