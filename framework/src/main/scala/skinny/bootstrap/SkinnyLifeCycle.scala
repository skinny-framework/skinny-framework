package skinny.bootstrap

import org.scalatra.LifeCycle
import javax.servlet.ServletContext

/**
 * LifeCycle for Skinny app.
 */
trait SkinnyLifeCycle extends LifeCycle {

  /**
   * Enables Skinny-ORM configuration if true.
   */
  val dbSettingsRequired: Boolean = true

  /**
   * Initializes Skinny framework application.
   *
   * This abstract method should be implemented to configure routes.
   *
   * @param ctx servlet context
   */
  def initSkinnyApp(ctx: ServletContext): Unit

  override def init(ctx: ServletContext): Unit = {
    if (dbSettingsRequired) skinny.DBSettings.initialize()
    initSkinnyApp(ctx)
  }

  override def destroy(context: ServletContext) {
    if (dbSettingsRequired) skinny.DBSettings.destroy()
    super.destroy(context)
  }

}
