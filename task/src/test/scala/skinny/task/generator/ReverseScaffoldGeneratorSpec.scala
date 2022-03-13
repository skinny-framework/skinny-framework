package skinny.task.generator

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ReverseScaffoldGeneratorSpec extends AnyFunSpec with Matchers {

  describe("ReverseScaffoldGenerator") {
    it("should be available") {
      ReverseScaffoldGenerator.run("", Nil, Some("test"))
    }
  }
}
