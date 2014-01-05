import _root_.controller._
import skinny._
import skinny.controller.AssetsController

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {
    // Notice: Enables open-session-in-view pattern
    //ctx.mount(classOf[skinny.orm.servlet.TxPerRequestFilter], "/*")
    Controllers.root.mount(ctx)
    Controllers.programmers.mount(ctx)
    SnakeCaseKeyExamplesController.mount(ctx)
    CompaniesController.mount(ctx)
    SkillsController.mount(ctx)
    Controllers.mustache.mount(ctx)
    Controllers.thymeleaf.mount(ctx)
    AssetsController.mount(ctx)
  }

}
