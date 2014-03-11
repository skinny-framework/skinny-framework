package skinny.worker

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.logging.Logging

class SkinnyWorkerServiceSpec extends FunSpec with ShouldMatchers with Logging {

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
      Thread.sleep(500L)
      service.shutdownNow()
      counter should be > (5)
    }

  }

}

