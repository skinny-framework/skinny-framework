package skinny.task

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SkinnyTaskLauncherSpec extends AnyFlatSpec with Matchers {

  val launcher       = new SkinnyTaskLauncher {}
  var result: String = "ng"

  it should "accept registered tasks" in {
    launcher.register("echo", (params) => params.foreach(println))
    launcher.register("save", (params) => params.headOption.foreach(p => result = p))

    launcher.main(Array("save", "ok"))
    result should equal("ok")
  }

  it should "show usage" in {
    launcher.showUsage
  }

}
