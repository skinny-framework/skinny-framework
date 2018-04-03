package skinny.bootstrap

import javax.servlet.ServletContext
import skinny.micro.LifeCycle
import skinny.micro.routing.RouteRegistry
import skinny.worker._

/**
  * LifeCycle for Skinny app.
  */
trait SkinnyLifeCycle extends LifeCycle {

  /**
    * Enables Skinny-ORM configuration if true.
    */
  def dbSettingsEnabled: Boolean = true

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
    RouteRegistry.init()
    initSkinnyApp(ctx)
  }

  override def destroy(context: ServletContext): Unit = {
    if (dbSettingsEnabled) skinny.DBSettings.destroy()
    if (workerServiceEnabled) skinnyWorkerService.shutdownNow()
    RouteRegistry.init()
    super.destroy(context)
  }

}
