package skinny.task.generator

import org.scalatest._

class ReverseScaffoldGeneratorSpec extends FunSpec with Matchers {

  describe("ReverseScaffoldGenerator") {
    it("should be available") {
      ReverseScaffoldGenerator.run("", Nil, Some("test"))
    }
  }
}
