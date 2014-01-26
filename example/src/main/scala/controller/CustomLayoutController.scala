package controller

class CustomLayoutController extends ApplicationController {

  def index = layout("/simple/foo.scaml").render("/custom-layout/index")

  def default = render("/custom-layout/default")

  def bar = {
    layout("/bar.scaml")
    render("/custom-layout/bar")
  }

}
