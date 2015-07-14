package sample

import model.AppName
import scaldi.Module
import service.{ EchoService, EchoServiceImpl }
import skinny._
import skinny.controller.feature.ScaldiFeature
import skinny.test.scalatest.SkinnyFlatSpec

class OverrideInjectionSpec extends SkinnyFlatSpec {

  System.setProperty(SkinnyEnv.PropertyKey, SkinnyEnv.Test)

  behavior of "Controllers with overriden Scaldi"

  object ServicesModule extends Module {
    bind[EchoService] to new EchoServiceImpl
  }

  class AppModule extends Module {
    bind[AppName] to AppName("ScaldiExample")
  }

  trait ApplicationController extends SkinnyController with ScaldiFeature {
    override def scaldiModules = Seq(ServicesModule, new AppModule)
  }
  class ModulesController extends ApplicationController {
    def index = {
      inject[EchoService].echo(params.getAs[String]("echo").getOrElse(""))
    }
    def appName = inject[AppName].value
    def env = inject[SkinnyEnv].getOrElse("xxx")
  }
  object modules extends ModulesController with Routes {
    get("/foo/")(index).as('index)
    get("/foo/appName")(appName).as('appName)
    get("/foo/env")(env).as('env)
  }
  object module extends SkinnyController with ScaldiFeature with Routes {
    override def scaldiModules = Seq(new AppModule)
    def index = inject[AppName].value
    get("/bar/name")(index).as('index)
  }
  addFilter(modules, "/*")
  addFilter(module, "/*")

  "Scaldi injector by method overriding" should "work" in {
    get("/foo/") {
      status should equal(200)
      body should equal("")
    }
    get("/foo/?echo=foo") {
      status should equal(200)
      body should equal("foo")
    }
    get("/foo/appName") {
      status should equal(200)
      body should equal("ScaldiExample")
    }
    get("/foo/env") {
      status should equal(200)
      body should equal("test")
    }
    get("/bar/name") {
      status should equal(200)
      body should equal("ScaldiExample")
    }
  }

}
