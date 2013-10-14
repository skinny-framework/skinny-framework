package controller

import skinny._

object Controllers {

  val root = new RootController with Routes {
    get("/?")(index).as('index)
  }

}

