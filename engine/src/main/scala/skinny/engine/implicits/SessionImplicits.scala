package skinny.engine.implicits

import scala.language.implicitConversions

import javax.servlet.http.HttpSession
import skinny.engine.context.SkinnyEngineContext

/**
 * This trait provides session support for stateful applications.
 */
trait SessionImplicits { self: ServletApiImplicits =>

  /**
   * The current session.  Creates a session if none exists.
   */
  implicit def session(implicit ctx: SkinnyEngineContext): HttpSession = ctx.request.getSession

  def session(key: String)(implicit ctx: SkinnyEngineContext): Any = session(ctx)(key)

  def session(key: Symbol)(implicit ctx: SkinnyEngineContext): Any = session(ctx)(key)

  /**
   * The current session.  If none exists, None is returned.
   */
  def sessionOption(implicit ctx: SkinnyEngineContext): Option[HttpSession] = Option(ctx.request.getSession(false))

}
