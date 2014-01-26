package skinny.controller

import org.scalatra.test.scalatest.ScalatraFlatSpec
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import skinny.Format
import org.scalatest.BeforeAndAfter
import java.io.File

/**
 * Author: chris
 * Created: 1/26/14
 */
class ScalateTemplateEngineFeatureSpec extends ScalatraFlatSpec with BeforeAndAfter {

  behavior of "ScalateTemplateEngineFeature"

  val resourcesDir = "framework/src/test/resources"
  servletContextHandler.setResourceBase(resourcesDir)

  def templateFiles = new File(resourcesDir, "WEB-INF/views/foo").listFiles()

  object SspController extends SkinnyController {
    // scalateExtension is "ssp" by default
    def a = render("foo/a")
    def c = render("foo/c")
    def xyz = render("foo/xyz")

    get("/ssp/a")(a)
    get("/ssp/c")(c)
    get("/ssp/xyz")(xyz)
  }

  object JadeController extends SkinnyController {
    override def scalateExtension: String = "jade"
    def b = render("foo/b")
    def c = render("foo/c")
    get("/jade/b")(b)
    get("/jade/c")(c)
  }

  addFilter(SspController, "/*")
  addFilter(JadeController, "/*")

  before {
    // Delete any auto-generated templates
    for (f <- templateFiles.filter(_.getName.startsWith("xyz"))) {
      f.delete()
    }
  }

  it should "render an SSP template" in {
    get("/ssp/a") {
      status should be(200)
      body should include ("<p>This is SSP template A")
    }
  }

  it should "render a Jade template" in {
    get("/jade/b") {
      status should be(200)
      body should include ("<p>This is Jade template B")
    }
  }

  it should "render a Jade template when there is also an SSP template available" in {
    get("/jade/c") {
      status should be(200)
      body should include ("<p>This is Jade template C")
    }
  }

  it should "render an SSP template when there is also a Jade template available" in {
    get("/ssp/c") {
      status should be(200)
      body should include ("<p>This is SSP template C")
    }
  }

  it should "auto-generate a template when no matching template is found" in {
    // Check that auto-generated template does not exist yet
    templateFiles.map(_.getName) should not contain "xyz.html.ssp"

    get("/ssp/xyz") {
      status should be(200)
      body should include ("This is an auto-generated file")
    }
  }

}
