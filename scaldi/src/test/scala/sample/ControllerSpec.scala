package sample

import skinny._
import scaldi._
import skinny.controller.feature.ScaldiFeature
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ControllerSpec extends ScalatraFlatSpec {

  System.setProperty(SkinnyEnv.PropertyKey, SkinnyEnv.Test)

  behavior of "Controllers with Scaldi"

  trait EchoService {
    def echo(msg: String): String
  }
  class EchoServiceImpl extends EchoService {
    override def echo(msg: String) = msg
  }
  object ControllerModule extends Module {
    bind[EchoService] to new EchoServiceImpl
  }

  case class AppName(value: String)
  class ControllerModule2 extends Module {
    bind[AppName] to AppName("ScaldiExample")
  }

  trait ApplicationController extends SkinnyController with ScaldiFeature {
    def scaldiModules = Seq(ControllerModule, new ControllerModule2)
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
    def scaldiModules = Seq(new ControllerModule2)
    def index = inject[AppName].value
    get("/bar/name")(index).as('index)
  }
  addFilter(modules, "/*")
  addFilter(module, "/*")

  "Scaldi injector" should "work" in {
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
