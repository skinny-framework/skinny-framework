package integrationtest

import controller.Controllers
import skinny.test.SkinnyTestSupport
import skinny.test.scalatest.SkinnyFlatSpec
import unit.DBSettings

class FileUploadControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport with DBSettings {

  addServlet(Controllers.fileUpload, "/*")

  it should "redirect users as expected" in {
    post("/fileupload/submit", "name" -> "foo") {
      status should equal(302)
    }
  }

}
