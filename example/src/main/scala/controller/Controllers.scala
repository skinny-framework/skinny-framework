package controller

import skinny._

object Controllers {

  val root = new RootController with Routes {
    get("/?")(index).as('index)
    get("/session/renew")(renewSessionAttributes).as('sessionRenew)
  }

  val programmers = new ProgrammersController with Routes {
    post("/programmers/:programmerId/company/:companyId")(joinCompany).as('joinCompany)
    delete("/programmers/:programmerId/company")(leaveCompany).as('leaveCompany)
    post("/programmers/:programmerId/skills/:skillId")(addSkill).as('addSkill)
    delete("/programmers/:programmerId/skills/:skillId")(deleteSkill).as('deleteSkill)
  }

  val thymeleaf = new ThymeleafController with Routes {
    get("/thymeleaf/?")(index).as('index)
  }

}

