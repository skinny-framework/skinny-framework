package controller
import org.scalatest._
import skinny.test._
class FileUploadControllerSpec extends FunSpec with Matchers {
  def createMockController = new FileUploadController with MockServlet
  describe("FileUploadController") {
    it("should work with MockServlet") {
      try {
        val controller = createMockController
        controller.form
        controller.status should equal(200)
      } catch {
        case e: Exception =>
          e.printStackTrace
          throw e
      }
    }
  }
}
