package integrationtest

import controller.{ Controllers, ErrorController }
import model.Company
import org.scalatra.test.scalatest._
import skinny.test.SkinnyTestSupport
import unit.DBSettings

class FileUploadControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {

  addServlet(Controllers.fileUpload, "/*")

  it should "return error as expected" in {
    post("/fileupload/submit", "name" -> "foo") {
      status should equal(302)
    }
  }

}
