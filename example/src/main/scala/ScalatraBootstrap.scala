import _root_.controller._
import skinny._
import skinny.controller.{ AssetsController }
import skinny.filter.{ ErrorPageFilter }
import skinny.servlet.filter.ExceptionLoggingNotifier

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    // enables open-session-in-view pattern
    //ctx.mount(classOf[skinny.orm.servlet.TxPerRequestFilter], "/*")

    // error filter example
    ctx.mount(classOf[ExceptionLoggingNotifier], "/*")

    Controllers.root.mount(ctx)
    Controllers.programmers.mount(ctx)
    CompaniesController.mount(ctx)
    CommentsController.mount(ctx)
    SkillsController.mount(ctx)
    SnakeCaseKeyExamplesController.mount(ctx)
    Controllers.mustache.mount(ctx)
    Controllers.thymeleaf.mount(ctx)

    AssetsController.mount(ctx)
  }

}
