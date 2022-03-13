package skinny.task

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DefaultTaskLauncherSpec extends AnyFlatSpec with Matchers {

  it should "be available" in {
    val launcher = new DefaultTaskLauncher {}
    launcher.main(Array())
  }

}
