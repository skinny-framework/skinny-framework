package skinny.view.velocity

import javax.servlet.ServletContext
import org.apache.velocity.tools.view.VelocityView

/**
  * VelocityView configuration factory.
  */
object VelocityViewConfig {

  def viewWithServletContext(ctx: ServletContext, sbtProjectPath: Option[String] = None): VelocityView = {
    new ScalaVelocityView(ctx, sbtProjectPath)
  }

}
