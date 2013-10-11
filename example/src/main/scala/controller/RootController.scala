package controller

class RootController extends ApplicationController {

  def index = render("/root/index")

  def renewSessionAttributes = {
    val locale = params.getAs[String]("locale").filter(_.length > 0).orNull[String]
    setCurrentLocale(locale)
    redirect(params.getAs[String]("returnTo").map(_.replaceFirst(s"^${contextPath}", "")).getOrElse("/"))
  }

}
