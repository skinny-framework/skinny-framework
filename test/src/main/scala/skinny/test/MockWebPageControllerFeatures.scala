package skinny.test

import skinny.Format
import skinny.controller.SkinnyWebPageControllerFeatures

/**
 * Mock of SkinnyWebPageControllerFeatures.
 */
trait MockWebPageControllerFeatures { self: MockControllerBase with SkinnyWebPageControllerFeatures =>

  var renderCall: Option[RenderCall] = None
  override def render(path: String)(implicit format: Format = Format.HTML): String = {
    // If Content-Type is already set, never overwrite it.
    if (contentType == null) {
      contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse("")
    }
    renderCall = Option(RenderCall(path))
    "Valid response body won't be returned from MockController. " +
      "When you'd like to verify response body, use Scalatra tests with embedded Jetty instead."
  }

}
