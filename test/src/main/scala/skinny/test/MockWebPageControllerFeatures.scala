package skinny.test

import skinny.Format
import skinny.controller.SkinnyWebPageControllerFeatures
import skinny.micro.context.SkinnyContext

/**
  * Mock of SkinnyWebPageControllerFeatures.
  */
trait MockWebPageControllerFeatures { self: MockControllerBase with SkinnyWebPageControllerFeatures =>

  var renderCall: Option[RenderCall] = None

  override def render(path: String)(
      implicit ctx: SkinnyContext = skinnyContext,
      format: Format = Format.HTML
  ): String = {
    // If Content-Type is already set, never overwrite it.
    if (contentType(ctx) == null) {
      (contentType = format.contentType + charset.map(c => s"; charset=${c}").getOrElse(""))(ctx)
    }
    renderCall = Option(RenderCall(path))
    "Valid response body won't be returned from MockController. " +
    "When you'd like to verify response body, use Scalatra tests with embedded Jetty instead."
  }

}
