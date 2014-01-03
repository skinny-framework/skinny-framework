package controller

import skinny._

object Controllers {

  object root extends RootController with Routes {
    val indexUrl = get("/?")(index).as('index)
    val sessionRenewUrl = get("/session/renew")(renewSessionAttributes).as('sessionRenew)
  }

  object programmers extends ProgrammersController with Routes {
    val joinCompanyUrl = post("/programmers/:programmerId/company/:companyId")(joinCompany).as('joinCompany)
    val leaveCompanyUrl = delete("/programmers/:programmerId/company")(leaveCompany).as('leaveCompany)
    val addSkillUrl = post("/programmers/:programmerId/skills/:skillId")(addSkill).as('addSkill)
    val deleteSkillUrl = delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as('deleteSkill)
  }

  object mustache extends MustacheController with Routes {
    val indexUrl = get("/mustache/?")(index).as('index)
  }

  object thymeleaf extends ThymeleafController with Routes {
    val indexUrl = get("/thymeleaf/?")(index).as('index)
  }

}

