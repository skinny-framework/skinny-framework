import skinny._
import skinny.controller._
import _root_.controller._

class ScalatraBootstrap extends SkinnyLifeCycle {

  // If you prefer more logging, configure this settings 
  scalikejdbc.GlobalSettings.loggingSQLAndTime = scalikejdbc.LoggingSQLAndTimeSettings(
    singleLineMode = true
  )

  override def initSkinnyApp(ctx: ServletContext) {
    Controllers.root.mount(ctx)
    AssetsController.mount(ctx)
  }

}

