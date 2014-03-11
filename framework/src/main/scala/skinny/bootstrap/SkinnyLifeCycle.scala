package skinny.bootstrap

import org.scalatra.LifeCycle
import javax.servlet.ServletContext
import skinny.worker._

/**
 * LifeCycle for Skinny app.
 */
trait SkinnyLifeCycle extends LifeCycle {

  /**
   * Enables Skinny-ORM configuration if true.
   */
  def dbSettingsEnabled: Boolean = true

  @deprecated("Use #dbSettingsEnabled instead", since = "1.0.0")
  def dbSettingsRequired: Boolean = dbSettingsEnabled

  /**
   * Enables SkinnyWorkerService if true.
   */
  def workerServiceEnabled: Boolean = true

  /**
   * SkinnyWorkerService
   */
  lazy val skinnyWorkerService = {
    if (workerServiceEnabled) new SkinnyWorkerService()
    else throw new IllegalStateException("SkinnyWorkerService is disabled now. Turn on #workerServiceEnabled!")
  }

  /**
   * Initializes Skinny framework application.
   *
   * This abstract method should be implemented to configure routes.
   *
   * @param ctx servlet context
   */
  def initSkinnyApp(ctx: ServletContext): Unit

  override def init(ctx: ServletContext): Unit = {
    if (dbSettingsEnabled) skinny.DBSettings.initialize()
    initSkinnyApp(ctx)
  }

  override def destroy(context: ServletContext) {
    if (dbSettingsEnabled) skinny.DBSettings.destroy()
    if (workerServiceEnabled) skinnyWorkerService.shutdownNow()
    super.destroy(context)
  }

}
