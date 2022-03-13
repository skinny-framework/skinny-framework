package skinny.worker

import skinny.logging.Logging
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class SkinnyWorkerServiceSpec extends AnyFunSpec with Matchers with Logging {

  describe("SkinnyWorkerService") {

    var counter = 0
    val worker = new SkinnyWorker {
      def execute = {
        logger.debug("Hello World!")
        counter += 1
      }
    }

    it("should run") {
      val service = new SkinnyWorkerService()
      service.everyFixedMilliseconds(worker, 10)
      Thread.sleep(1000L)
      service.shutdownNow()
      counter should be > (5)
    }

  }

}
