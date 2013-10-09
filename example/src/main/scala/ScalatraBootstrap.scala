import _root_.controller._
import skinny._
import skinny.routing.Routes

class ScalatraBootstrap extends SkinnyLifeCycle {

  val root = new RootController with Routes {
    get("/?")(index).as('index)
    get("/session/renew")(renewSessionAttributes).as('sessionRenew)
  }

  val programmers = new ProgrammersController with Routes {
    post("/programmers/:programmerId/company/:companyId")(joinCompany).as('joinCompany)
    delete("/programmers/:programmerId/company")(leaveCompany).as('leaveCompany)
    post("/programmers/:programmerId/skills/:skillId")(addSkill).as('addSkill)
    delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as('deleteSkill)
  }

  override def initSkinnyApp(ctx: ServletContext) {
    /* verbose query logger
    import scalikejdbc._
    val className = "skinny.orm.formatter.HibernateSQLFormatter"
    GlobalSettings.sqlFormatter = SQLFormatterSettings(className)
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings()
     */
    DBInitializer.createTable()

    ctx.mount(root, "/*")
    ctx.mount(programmers, "/*")
    ctx.mount(CompaniesController, "/*")
    ctx.mount(SkillsController, "/*")
  }

}
