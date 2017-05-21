package sample

import skinny._
import skinny.controller.feature.ScaldiFeature
import service.EchoService
import model.AppName
import skinny.test.SkinnyFlatSpec

class ConfigInjectionSpec extends SkinnyFlatSpec {

  System.setProperty(SkinnyEnv.PropertyKey, SkinnyEnv.Test)

  behavior of "Controllers with Scaldi configuration"

  trait ApplicationController extends SkinnyController with ScaldiFeature

  class ModulesController extends ApplicationController {
    def index = {
      inject[EchoService].echo(params.getAs[String]("echo").getOrElse(""))
    }
    def appName = inject[AppName].value
    def env     = inject[SkinnyEnv].getOrElse("xxx")
  }

  object modules extends ModulesController with Routes {
    get("/foo/")(index).as('index)
    get("/foo/appName")(appName).as('appName)
    get("/foo/env")(env).as('env)
  }
  addFilter(modules, "/*")

  "Scaldi injector by configuration" should "work" in {
    get("/foo/") {
      println(body)
      status should equal(200)
      body should equal("")
    }
    get("/foo/?echo=foo") {
      status should equal(200)
      body should equal("FOO")
    }
    get("/foo/appName") {
      status should equal(200)
      body should equal("config-example")
    }
    get("/foo/env") {
      status should equal(200)
      body should equal("test")
    }
  }

}
