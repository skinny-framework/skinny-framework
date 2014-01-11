package controller

import skinny._
import skinny.filter.TxPerRequestFilter

object ErrorController extends ApplicationController with TxPerRequestFilter with Routes {

  def runtime = throw new RuntimeException

  val runtimeUrl = get("/error/runtime")(runtime).as('errorPage)

}
