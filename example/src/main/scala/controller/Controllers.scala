package controller

import skinny._
import skinny.controller.AssetsController

object Controllers {

  def mount(ctx: ServletContext): Unit = {

    ErrorController.mount(ctx)

    fileUpload.mount(ctx)

    root.mount(ctx)
    companies.mount(ctx)
    customLayout.mount(ctx)
    mail.mount(ctx)
    mustache.mount(ctx)
    programmers.mount(ctx)
    thymeleaf.mount(ctx)
    freemarker.mount(ctx)
    sampleApi.mount(ctx)
    scaldi.mount(ctx)
    dashboard.mount(ctx)

    SkillsController.mount(ctx)
    CommentsController.mount(ctx)
    SnakeCaseKeyExamplesController.mount(ctx)

    AssetsController.mount(ctx)
  }

  object companies extends CompaniesController {
  }

  object root extends RootController with Routes {
    val indexUrl = get("/")(index).as('index)
    val sessionRenewUrl = get("/session/renew")(renewSessionAttributes).as('sessionRenew)
    val errorUrl = get("/error")(errorExample).as('error)
    val reactUrl = get("/react")(reactExample).as('react)
    val invalidateUrl = get("/invalidate")(invalidateExample).as('invalidate)
  }

  object programmers extends ProgrammersController with Routes {
    val joinCompanyUrl = post("/programmers/:programmerId/company/:companyId")(joinCompany).as('joinCompany)
    val leaveCompanyUrl = delete("/programmers/:programmerId/company")(leaveCompany).as('leaveCompany)
    val addSkillUrl = post("/programmers/:programmerId/skills/:skillId")(addSkill).as('addSkill)
    val deleteSkillUrl = delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as('deleteSkill)
  }

  object customLayout extends CustomLayoutController with Routes {
    val indexUrl = get("/custom-layout/?".r)(index).as('index)
    val defaultUrl = get("/custom-layout/default")(default).as('default)
    val barUrl = get("/custom-layout/bar")(bar).as('bar)
  }

  object mail extends MailController with Routes {
    get("/mail/")(index)
    get("/mail/ssp")(ssp)
  }

  object mustache extends MustacheController with Routes {
    val indexUrl = get("/mustache/?".r)(index).as('index)
  }

  object thymeleaf extends ThymeleafController with Routes {
    val indexUrl = get("/thymeleaf/?".r)(index).as('index)
  }
  object freemarker extends FreeMarkerController with Routes {
    val indexUrl = get("/freemarker/?".r)(index).as('index)
  }

  object sampleApi extends SampleApiController with Routes {
    val createCompanyUrl = post("/api/companies")(createCompany).as('createCompany)
    val companiesUrl = get("/api/companies")(companiesJson).as('companies)
  }

  object fileUpload extends FileUploadController with Routes {
    val formUrl = get("/fileupload")(form).as('form)
    val submitUrl = post("/fileupload/submit")(submit).as('submit)
  }

  object scaldi extends ScaldiController with Routes {
    val indexUrl = get("/scaldi/")(index).as('index)
  }

  object dashboard extends DashboardController with Routes {
    val indexUrl = get("/dashboard/")(index).as('index)
  }

}

