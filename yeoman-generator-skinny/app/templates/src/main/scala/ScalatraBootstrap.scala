import skinny._
import _root_.controller._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {
    if (SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) {
      import scalikejdbc._, SQLInterpolation._
      lib.DBInitializer.runIfFailed(sql"select 1 from companies limit 1")
    }
    Controllers.root.mount(ctx)
    CompaniesController.mount(ctx)
    AssetsController.mount(ctx)
  }

}

