package controller

import skinny.SkinnyController

class RootController extends SkinnyController {

  def index = render("/root/index")

}
