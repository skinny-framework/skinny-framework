package controller

import skinny.SkinnyController

class RootController extends SkinnyController {

  def index = render("/root/index")

  def renewSessionAttributes = {
    val locale = params.getAs[String]("locale").filter(_.length > 0).orNull[String]
    setCurrentLocale(locale)
    redirect(params.getAs[String]("return_to").getOrElse("/"))
  }

}
