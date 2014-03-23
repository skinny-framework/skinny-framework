import _root_.controller._
import skinny._
import skinny.session.SkinnySessionInitializer

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    scalikejdbc.GlobalSettings.loggingSQLAndTime = scalikejdbc.LoggingSQLAndTimeSettings(
      singleLineMode = true
    )
    ctx.mount(classOf[SkinnySessionInitializer], "/*")

    Controllers.mount(ctx)
  }

}
