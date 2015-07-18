package integrationtest

import controller.Controllers
import skinny.test.{SkinnyFlatSpec, SkinnyTestSupport}
import unit.DBSettings

class FileDownloadControllerSpec extends SkinnyFlatSpec with SkinnyTestSupport with DBSettings {

  addFilter(Controllers.fileDownload, "/*")

  it should "return error as expected" in {
    get("/filedownload/small") {
      status should equal(200)
      body should equal("OK")
      header("Content-Type") should include("plain/text")
    }

    get("/filedownload/null") {
      status should equal(200)
      body should equal("")
      header("Content-Type") should include("plain/text")
    }

    get("/filedownload/error") {
      status should equal(500)
    }
  }

}
