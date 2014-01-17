package controller

import skinny._

object Controllers {

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

  object mustache extends MustacheController with Routes {
    val indexUrl = get("/mustache/?".r)(index).as('index)
  }

  object thymeleaf extends ThymeleafController with Routes {
    val indexUrl = get("/thymeleaf/?".r)(index).as('index)
  }

}

