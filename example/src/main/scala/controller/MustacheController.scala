package controller

import skinny.filter.TxPerRequestFilter

class MustacheController extends ApplicationController with TxPerRequestFilter {

  override def scalateExtension = "mustache"

  def index = {
    set("echo" -> params.get("echo"))
    render("/mustache/index")
  }

}
