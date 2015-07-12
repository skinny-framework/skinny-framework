package skinny.engine.context

import javax.servlet.ServletContext

trait MountConfig {

  def apply(ctxt: ServletContext)

}
