package skinny.engine.scalate

import java.io.PrintWriter
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import org.fusesource.scalate.{ Binding, RenderContext }
import skinny.engine.base.BeforeAfterDsl
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.i18n.{ Messages, I18nSupport }

trait ScalateI18nSupport
    extends ScalateSupport
    with I18nSupport { self: BeforeAfterDsl =>

  /*
   * Binding done here seems to work all the time.
   *
   * If it were placed in createRenderContext, it wouldn't work for "view" templates
   * on first access. However, on subsequent accesses, it worked fine.
   */
  before() {
    templateEngine.bindings ::= Binding("messages", classOf[Messages].getName, true, isImplicit = true)
  }

  /**
   * Added "messages" into the template context so it can be accessed like:
   * #{messages("hello")}
   */
  override protected def createRenderContext(
    req: HttpServletRequest,
    resp: HttpServletResponse,
    out: PrintWriter)(implicit ctx: SkinnyEngineContext): SkinnyEngineRenderContext = {

    val context = super.createRenderContext(req, resp, out)(ctx)
    context.attributes("messages") = messages(ctx)
    context
  }

}
