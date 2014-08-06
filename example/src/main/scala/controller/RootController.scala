package controller

import service._
import skinny.controller.Params
import skinny.filter._
import skinny.validator._

class RootController extends ApplicationController with TxPerRequestFilter with SkinnySessionFilter {

  val echoService: EchoService = EchoService()

  def index = {
    set("echo" -> params.get("echo").map(v => echoService.echo(v)))
    render("/root/index")
  }

  def renewSessionAttributes = {
    val locale = params.getAs[String]("locale").filter(_.length > 0).orNull[String]
    setCurrentLocale(locale)
    redirect(params.getAs[String]("returnTo").map(_.replaceFirst(s"^${contextPath}", "")).getOrElse("/"))
  }

  def errorExample = throw new RuntimeException("sample error!")

  def reactExample = render("/react/index")

  def invalidateExample = {
    session.invalidate()
    redirect("/")
  }

  def form = validation(Params(params),
    paramKey("foo") is required & matches(".*OK.*", "ok")
  )

  def nestedI18nExample = {
    if (form.validate()) {
      status = 200
    } else {
      status = 400
    }
    render("/nestI18nExample/index")
  }

  case class matches(regexp: String, regexpName: String) extends ValidationRule {
    override def name: String = "matches"
    override def messageParams = Seq(I18nKeyParam(regexpName))
    override def isValid(value: Any): Boolean = isEmpty(value) || value.toString.matches(regexp)
  }

}
