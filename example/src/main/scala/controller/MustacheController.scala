package controller

class MustacheController extends ApplicationController {

  override def scalateExtension = "mustache"

  def index = {
    set("echo" -> params.get("echo"))
    render("/mustache/index")
  }

}
