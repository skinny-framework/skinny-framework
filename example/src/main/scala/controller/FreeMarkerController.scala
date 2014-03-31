package controller

import skinny.controller.SkinnyController
import skinny.controller.feature._
import scala.beans.BeanProperty

class FreeMarkerController extends SkinnyController with FreeMarkerTemplateEngineFeature {

  override lazy val sbtProjectPath = Some("example")

  case class Person(id: Long, @BeanProperty name: String)

  def index() = {
    set("name", "Thymeleaf")
    set("numbers", Seq("one", "two", "three"))
    set("nestedNumbers", Seq(Seq(1, 2), Seq("ONE", "TWO")))
    set("map", Map("one" -> 1, "two" -> 2))
    set("persons", Seq(Person(1, "Alice"), Person(2, "Bob")))
    render("/freemarker/index")
  }

}
