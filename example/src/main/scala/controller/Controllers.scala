package controller

import skinny._
import skinny.controller.AssetsController

object Controllers {

  def mount(ctx: ServletContext): Unit = {

    ErrorController.mount(ctx)

    fileUpload.mount(ctx)
    fileDownload.mount(ctx)

    root.mount(ctx)
    companies.mount(ctx)
    customLayout.mount(ctx)
    mail.mount(ctx)
    mustache.mount(ctx)
    programmers.mount(ctx)
    thymeleaf.mount(ctx)
    freemarker.mount(ctx)
    sampleApi.mount(ctx)
    sampleTxApi.mount(ctx)
    dashboard.mount(ctx)

    SkillsController.mount(ctx)
    comments.mount(ctx)
    SnakeCaseKeyExamplesController.mount(ctx)

    AngularXHRProgrammersController.mount(ctx)
    angularApp.mount(ctx)

    facebook.mount(ctx)
    github.mount(ctx)
    google.mount(ctx)
    twitter.mount(ctx)
    typetalk.mount(ctx)
    dropbox.mount(ctx)
    backlog.mount(ctx)
    backlogJp.mount(ctx)

    AssetsController.mount(ctx)
  }

  object comments  extends CommentsController  {}
  object companies extends CompaniesController {}

  object root extends RootController with Routes {
    val indexUrl        = get("/")(index).as(Symbol("index"))
    val sessionRenewUrl = get("/session/renew")(renewSessionAttributes).as(Symbol("sessionRenew"))
    val errorUrl        = get("/error")(errorExample).as(Symbol("error"))
    val reactUrl        = get("/react")(reactExample).as(Symbol("react"))
    val nestedI18nUrl   = get("/nested-i18n")(nestedI18nExample).as(Symbol("nestedI18n"))
    val invalidateUrl   = get("/invalidate")(invalidateExample).as(Symbol("invalidate"))
  }

  object programmers extends ProgrammersController with Routes {
    val joinCompanyUrl  = post("/programmers/:programmerId/company/:companyId")(joinCompany).as(Symbol("joinCompany"))
    val leaveCompanyUrl = delete("/programmers/:programmerId/company")(leaveCompany).as(Symbol("leaveCompany"))
    val addSkillUrl     = post("/programmers/:programmerId/skills/:skillId")(addSkill).as(Symbol("addSkill"))
    val deleteSkillUrl  = delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as(Symbol("deleteSkill"))
  }

  object customLayout extends CustomLayoutController with Routes {
    val indexUrl   = get("/custom-layout/?".r)(index).as(Symbol("index"))
    val defaultUrl = get("/custom-layout/default")(default).as(Symbol("default"))
    val barUrl     = get("/custom-layout/bar")(bar).as(Symbol("bar"))
  }

  object mail extends MailController with Routes {
    val indexUrl = get("/mail/")(index).as(Symbol("index"))
    val sspUrl   = get("/mail/ssp")(ssp).as(Symbol("ssp"))
  }

  object mustache extends MustacheController with Routes {
    val indexUrl = get("/mustache/?".r)(index).as(Symbol("index"))
  }

  object thymeleaf extends ThymeleafController with Routes {
    val indexUrl = get("/thymeleaf/?".r)(index).as(Symbol("index"))
  }
  object freemarker extends FreeMarkerController with Routes {
    val indexUrl = get("/freemarker/?".r)(index).as(Symbol("index"))
  }

  object sampleApi extends SampleApiController with Routes {
    val createCompanyUrl = post("/api/companies")(createCompany).as(Symbol("createCompany"))
    val companiesUrl     = get("/api/companies")(companiesJson).as(Symbol("companies"))
  }
  object sampleTxApi extends SampleTxApiController with Routes {
    get("/api/error")(index).as(Symbol("index"))
  }

  object fileUpload extends FileUploadController with Routes {
    val formUrl   = get("/fileupload")(form).as(Symbol("form"))
    val submitUrl = post("/fileupload/submit")(submit).as(Symbol("submit"))
  }
  object fileDownload extends FileDownloadController with Routes {
    val indexUrl = get("/filedownload")(index).as(Symbol("index"))
    val smallUrl = get("/filedownload/small")(small).as(Symbol("small"))
    val nullUrl  = get("/filedownload/null")(nullValue).as(Symbol("null"))
    val errorUrl = get("/filedownload/error")(error).as(Symbol("error"))
  }

  object dashboard extends DashboardController with Routes {
    val indexUrl = get("/dashboard/")(index).as(Symbol("index"))
  }

  object angularApp extends AngularAppController with Routes {
    val indexUrl       = get("/angular/app")(index).as(Symbol("index"))
    val programmersUrl = get("/angular/programmers/")(programmers).as(Symbol("programmers"))
  }

  object facebook extends FacebookController with Routes {
    val loginUrl    = get("/facebook")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/facebook/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/facebook/ok")(ok).as(Symbol("ok"))
  }

  object github extends GitHubController with Routes {
    val loginUrl    = get("/github")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/github/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/github/ok")(ok).as(Symbol("ok"))
  }

  object google extends GoogleController with Routes {
    val loginUrl    = get("/google")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/google/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/google/ok")(ok).as(Symbol("ok"))
  }

  object twitter extends TwitterController with Routes {
    val loginUrl    = get("/twitter")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/twitter/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/twitter/ok")(ok).as(Symbol("ok"))
  }

  object typetalk extends TypetalkController with Routes {
    val loginUrl    = get("/typetalk")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/typetalk/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/typetalk/ok")(ok).as(Symbol("ok"))
  }

  object dropbox extends DropboxController with Routes {
    val loginUrl    = get("/dropbox")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/dropbox/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/dropbox/ok")(ok).as(Symbol("ok"))
  }

  object backlog extends BacklogController with Routes {
    val loginUrl    = get("/backlog")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/backlog/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/backlog/ok")(ok).as(Symbol("ok"))
  }
  object backlogJp extends BacklogJPController with Routes {
    val loginUrl    = get("/backlogjp")(loginRedirect).as(Symbol("login"))
    val callbackUrl = get("/backlogjp/callback")(callback).as(Symbol("callback"))
    val okUrl       = get("/backlogjp/ok")(ok).as(Symbol("ok"))
  }

}
