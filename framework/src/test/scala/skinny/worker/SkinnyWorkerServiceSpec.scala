package skinny.worker

import org.scalatest._
import skinny.logging.Logging

class SkinnyWorkerServiceSpec extends FunSpec with Matchers with Logging {

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

