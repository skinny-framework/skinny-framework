package controller

import skinny.controller.feature.AngularXSRFCookieProviderFeature

class AngularAppController extends ApplicationController with AngularXSRFCookieProviderFeature {

  def index = layout("angular.ssp").render("/angular/app")

  def programmers = render("/angular/programmers/index")

}
