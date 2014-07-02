package controller

import service.EchoService
import skinny.controller.feature.ScaldiFeature

class ScaldiController extends ApplicationController with ScaldiFeature {

  override def scaldiModules = Seq(lib.AppModule)

  def index = {
    val value = params.getAs[String]("value").getOrElse("")
    inject[EchoService].echo(value)
  }

}
