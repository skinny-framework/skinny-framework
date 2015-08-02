package skinny.engine.context

import javax.servlet.ServletContext

/**
 * Servlet mount config.
 */
trait MountConfig {

  def apply(ctx: ServletContext)

}
