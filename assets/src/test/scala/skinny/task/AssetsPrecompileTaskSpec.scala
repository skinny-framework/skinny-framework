package skinny.task

import org.scalatest._

class AssetsPrecompileTaskSpec extends FlatSpec with Matchers {

  it should "be available" in {
    AssetsPrecompileTask.main(Array("tmp"))
  }

}
