package controller

class CustomLayoutController extends ApplicationController {

  override def scalateExtensions = List("scaml")

  layout("/simple/foo")

  def index = layout("/simple/foo").render("/custom-layout/index")

  def default = render("/custom-layout/default")

  def bar = {
    layout("/bar")
    render("/custom-layout/bar")
  }

}
