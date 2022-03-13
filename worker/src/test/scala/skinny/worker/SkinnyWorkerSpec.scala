package skinny.worker

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SkinnyWorkerSpec extends AnyFlatSpec with Matchers {
  behavior of "SkinnyWorker"

  it should "run" in {
    var called = false
    val worker = new SkinnyWorker {
      override def execute(): Unit = called = true
    }
    worker.run()

    called should equal(true)
  }
}
