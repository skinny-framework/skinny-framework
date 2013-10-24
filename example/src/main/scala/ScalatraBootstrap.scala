import _root_.controller._
import skinny._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    if (SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) {
      /* verbose query logger
      import scalikejdbc._
      GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings()
       */
      dev.DBInitializer.initialize()
    }

    // Enables open-session-in-view pattern
    //ctx.mount(classOf[skinny.servlet.TxPerRequestFilter], "/*")

    ctx.mount(Controllers.root, "/*")

    ctx.mount(Controllers.programmers, "/programmers/*")
    ctx.mount(Controllers.programmers, "/programmers.xml")
    ctx.mount(Controllers.programmers, "/programmers.json")

    ctx.mount(CompaniesController, "/companies/*")
    ctx.mount(CompaniesController, "/companies.xml")
    ctx.mount(CompaniesController, "/companies.json")

    ctx.mount(SkillsController, "/skills/*")
    ctx.mount(SkillsController, "/skills.xml")
    ctx.mount(SkillsController, "/skills.json")

    ctx.mount(Controllers.thymeleaf, "/thymeleaf/*")
  }

}
