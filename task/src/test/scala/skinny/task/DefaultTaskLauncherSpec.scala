package skinny.task

import org.scalatest._

class DefaultTaskLauncherSpec extends FlatSpec with Matchers {

  it should "be available" in {
    val launcher = new DefaultTaskLauncher {}
    launcher.main(Array())
  }

}
