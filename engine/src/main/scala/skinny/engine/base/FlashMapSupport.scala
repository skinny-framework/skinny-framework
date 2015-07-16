package skinny.engine.base

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.{ Handler, SkinnyEngineBase }
import skinny.engine.context.SkinnyEngineContext
import skinny.engine.flash.FlashMap
import skinny.engine.implicits.{ ServletApiImplicits, SessionImplicits }

object FlashMapSupport {

  val SessionKey = FlashMapSupport.getClass.getName + ".flashMap"

  val LockKey = FlashMapSupport.getClass.getName + ".lock"

  val FlashMapKey = "skinny.engine.FlashMap"

}

/**
 * Allows an action to set key-value pairs in a transient state that is accessible only to the next action and is expired immediately after that.
 * This is especially useful when using the POST-REDIRECT-GET pattern to trace the result of an operation.
 * {{{
 * post("/article/create") {
 *   // create session
 *   flash("notice") = "article created succesfully"
 *   redirect("/home")
 * }
 * get("/home") {
 *   // this will access the value set in previous action
 *   stuff_with(flash("notice"))
 * }
 * }}}
 * @see FlashMap
 */
trait FlashMapSupport
    extends Handler
    with ServletContextAccessor
    with SkinnyEngineContextInitializer
    with ServletApiImplicits
    with SessionImplicits {

  import FlashMapSupport._

  abstract override def handle(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    withRequest(req) {
      val context = SkinnyEngineContext.build(servletContext, req, res)
      val f = flash(context)
      val isOutermost = !req.contains(LockKey)

      SkinnyEngineBase.onCompleted { _ =>
        /*
         * http://github.com/scalatra/scalatra/issues/41
         * http://github.com/scalatra/scalatra/issues/57
         *
         * Only the outermost FlashMapSupport sweeps it at the end.
         * This deals with both nested filters and redirects to other servlets.
         */
        if (isOutermost) {
          f.sweep()
        }
        flashMapSetSession(f)(context)
      }(context)

      if (isOutermost) {
        req(LockKey) = "locked"
        if (sweepUnusedFlashEntries(req)) {
          f.flag()
        }
      }

      super.handle(req, res)
    }
  }

  /**
   * Override to implement custom session retriever, or sanity checks if session is still active
   * @param f
   */
  def flashMapSetSession(f: FlashMap)(implicit ctx: SkinnyEngineContext): Unit = {
    try {
      // Save flashMap to Session after (a session could stop existing during a request, so catch exception)
      session(ctx)(SessionKey) = f
    } catch {
      case e: Throwable =>
    }
  }

  private[this] def getFlash(implicit ctx: SkinnyEngineContext): FlashMap =
    ctx.request.get(SessionKey).map(_.asInstanceOf[FlashMap]).getOrElse {
      val map = session(ctx).get(SessionKey).map {
        _.asInstanceOf[FlashMap]
      }.getOrElse(new FlashMap)

      ctx.request.setAttribute(SessionKey, map)
      map
    }

  /**
   * Returns the [[FlashMap]] instance for the current request.
   */
  def flash(implicit ctx: SkinnyEngineContext): FlashMap = getFlash(ctx)

  def flash(key: String)(implicit ctx: SkinnyEngineContext): Any = getFlash(ctx)(key)

  /**
   * Determines whether unused flash entries should be swept.  The default is false.
   */
  protected def sweepUnusedFlashEntries(req: HttpServletRequest): Boolean = false

}
