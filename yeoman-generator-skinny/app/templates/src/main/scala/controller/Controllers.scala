package controller

import skinny._

object Controllers {
  object root extends RootController with Routes {
    val indexUrl = get("/?")(index).as('index)
  }
}

