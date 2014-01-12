import _root_.controller._
import skinny._
import skinny.controller._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {

    scalikejdbc.GlobalSettings.loggingSQLAndTime = scalikejdbc.LoggingSQLAndTimeSettings(
      singleLineMode = true
    )

    Controllers.root.mount(ctx)
    ErrorController.mount(ctx)

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
