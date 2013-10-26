import _root_.controller._
import skinny._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    if (SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) {
      // Notice: if you'd like to use verbose query logger
      // import scalikejdbc._
      // GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings()
      lib.DBInitializer.initialize()
    }
    // Notice: Enables open-session-in-view pattern
    //ctx.mount(classOf[skinny.servlet.TxPerRequestFilter], "/*")

    Controllers.root.mount(ctx)
    Controllers.programmers.mount(ctx)
    CompaniesController.mount(ctx)
    SkillsController.mount(ctx)
    Controllers.thymeleaf.mount(ctx)
    AssetsController.mount(ctx)
  }

}
