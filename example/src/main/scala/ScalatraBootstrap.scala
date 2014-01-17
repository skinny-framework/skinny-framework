import _root_.controller._
import skinny._
import skinny.controller._
import skinny.session.SkinnySessionInitializer

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    scalikejdbc.GlobalSettings.loggingSQLAndTime = scalikejdbc.LoggingSQLAndTimeSettings(
      singleLineMode = true
    )
    ctx.mount(classOf[SkinnySessionInitializer], "/*")

    SkillsController.mount(ctx)
    Controllers.root.mount(ctx)
    ErrorController.mount(ctx)
    Controllers.programmers.mount(ctx)
    CompaniesController.mount(ctx)
    CommentsController.mount(ctx)
    SnakeCaseKeyExamplesController.mount(ctx)
    Controllers.customLayout.mount(ctx)
    Controllers.mustache.mount(ctx)
    Controllers.thymeleaf.mount(ctx)

    AssetsController.mount(ctx)
  }

}
