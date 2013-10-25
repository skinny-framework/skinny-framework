package controller

import skinny.controller.SkinnyController
import skinny.controller.feature.ThymeleafTemplateEngineFeature

class ThymeleafController extends SkinnyController with ThymeleafTemplateEngineFeature {

  def index() = {
    set("name", "Thymeleaf")
    render("/thymeleaf/index")
  }

}
