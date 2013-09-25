import _root_.controller._
import skinny._
import skinny.routing.Routes

class ScalatraBootstrap extends SkinnyLifeCycle {

  def rootController = new RootController with Routes {
    get("/?")(index).as('index)
  }

  val programmers = new ProgrammersController with Routes {
    post("/programmers/:programmerId/company/:companyId")(joinCompany).as('joinCompany)
    delete("/programmers/:programmerId/company")(leaveCompany).as('leaveCompany)
    post("/programmers/:programmerId/skills/:skillId")(addSkill).as('addSkill)
    delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as('deleteSkill)
  }

  override def initSkinnyApp(ctx: ServletContext) {
    DBInitializer.createTable()
    ctx.mount(rootController, "/*")
    ctx.mount(CompaniesController, "/*")
    ctx.mount(programmers, "/*")
    ctx.mount(SkillsController, "/*")
  }

}
