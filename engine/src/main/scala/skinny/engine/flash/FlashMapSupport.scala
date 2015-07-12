package skinny.engine.flash

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

import skinny.engine.{ Handler, SkinnyEngineBase }

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
trait FlashMapSupport extends Handler {
  this: SkinnyEngineBase =>

  import FlashMapSupport._

  abstract override def handle(req: HttpServletRequest, res: HttpServletResponse): Unit = {
    withRequest(req) {
      val f = flash
      val isOutermost = !request.contains(LockKey)

      SkinnyEngineBase onCompleted { _ =>
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
        flashMapSetSession(f)
      }

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
  def flashMapSetSession(f: FlashMap): Unit = {
    try {
      // Save flashMap to Session after (a session could stop existing during a request, so catch exception)
      session(SessionKey) = f
    } catch {
      case e: Throwable =>
    }
  }

  private[this] def getFlash(req: HttpServletRequest): FlashMap =
    req.get(SessionKey).map(_.asInstanceOf[FlashMap]).getOrElse {
      val map = session.get(SessionKey).map {
        _.asInstanceOf[FlashMap]
      }.getOrElse(new FlashMap)

      req.setAttribute(SessionKey, map)
      map
    }

  /**
   * Returns the [[FlashMap]] instance for the current request.
   */
  def flash(implicit request: HttpServletRequest): FlashMap = getFlash(request)

  def flash(key: String)(implicit request: HttpServletRequest): Any = getFlash(request)(key)

  /**
   * Determines whether unused flash entries should be swept.  The default is false.
   */
  protected def sweepUnusedFlashEntries(req: HttpServletRequest): Boolean = false

}
