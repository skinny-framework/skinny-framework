package controller

import skinny.controller.SkinnyApiController
import model.Company

class SampleApiController extends SkinnyApiController {

  def companiesJson = toPrettyJSONString(Company.findAll())

}
