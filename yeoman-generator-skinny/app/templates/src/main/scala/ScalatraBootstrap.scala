import skinny._
import _root_.controller._
import _root_.model._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {
    if (SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) {
      import scalikejdbc._, SQLInterpolation._
      dev.DBInitializer.runIfFailed(sql"select 1 from companies limit 1")
    }
    ctx.mount(Controllers.root, "/*")
    ctx.mount(CompaniesController, "/companies/*")
  }

}

