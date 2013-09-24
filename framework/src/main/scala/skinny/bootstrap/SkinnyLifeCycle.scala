package skinny.bootstrap

import org.scalatra.LifeCycle
import javax.servlet.ServletContext

trait SkinnyLifeCycle extends LifeCycle {

  val dbSettingsRequired: Boolean = true

  override def init(ctx: ServletContext): Unit = {
    if (dbSettingsRequired) DBSettings.initialize()
    initSkinnyApp(ctx)
  }

  def initSkinnyApp(ctx: ServletContext): Unit

}
