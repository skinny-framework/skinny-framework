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

  object DefaultController extends SkinnyController {
    // by default, SSP templates are tried before Jade, so it should render SSP templates
    def a = render("foo/a")
    def c = render("foo/c")
    def xyz = render("foo/xyz")

    get("/default/a")(a)
    get("/default/c")(c)
    get("/default/xyz")(xyz)
  }

  object SspOnlyController extends SkinnyController {
    override def scalateExtensions = List("ssp")
    def a = render("foo/a")
    def c = render("foo/c")
    def xyz = render("foo/xyz")

    get("/ssp/a")(a)
    get("/ssp/c")(c)
    get("/ssp/xyz")(xyz)
  }

  object JadeOnlyController extends SkinnyController {
    override def scalateExtensions = List("jade")
    def b = render("foo/b")
    def c = render("foo/c")
    get("/jade/b")(b)
    get("/jade/c")(c)
  }

  object AnythingButSspController extends SkinnyController {
    override def scalateExtensions = List("mustache", "scaml", "jade")
    def b = render("foo/b")
    def c = render("foo/c")
    get("/anything-but-ssp/b")(b)
    get("/anything-but-ssp/c")(c)
  }

  object CustomLayoutController extends SkinnyController {
    override def scalateExtensions = List("ssp")

    def a = {
      layout("custom.jade")
      render("foo/a")
    }

    get("/custom-layout/a")(a)
  }

  addFilter(DefaultController, "/*")
  addFilter(SspOnlyController, "/*")
  addFilter(JadeOnlyController, "/*")
  addFilter(AnythingButSspController, "/*")
  addFilter(CustomLayoutController, "/*")

  before {
    // Delete any auto-generated templates
    for (f <- templateFiles.filter(_.getName.startsWith("xyz"))) {
      f.delete()
    }
  }

  it should "render an SSP template" in {
    get("/ssp/a") {
      status should be(200)
      body should include("<p>This is SSP template A")
    }
  }

  it should "render a Jade template" in {
    get("/jade/b") {
      status should be(200)
      body should include("<p>This is Jade template B")
    }
  }

  it should "render a Jade template when there is also an SSP template available" in {
    get("/jade/c") {
      status should be(200)
      body should include("<p>This is Jade template C")
    }
  }

  it should "render an SSP template when there is also a Jade template available" in {
    get("/ssp/c") {
      status should be(200)
      body should include("<p>This is SSP template C")
    }
  }

  it should "render an SSP template by default when there is a choice of templates" in {
    get("/default/c") {
      status should be(200)
      body should include("<p>This is SSP template C")
    }
  }

  it should "render a Jade template even if it not its first choice" in {
    get("/anything-but-ssp/b") {
      status should be(200)
      body should include("<p>This is Jade template B")
    }
    get("/anything-but-ssp/c") {
      status should be(200)
      body should include("<p>This is Jade template C")
    }
  }

  it should "auto-generate a template when no matching template is found" in {
    // Check that auto-generated template does not exist yet
    templateFiles.map(_.getName) should not contain "xyz.html.ssp"

    get("/ssp/xyz") {
      status should be(200)
      body should include("This is an auto-generated file")
    }
  }

  it should "support view and layout templates in different languages" in {
    get("/custom-layout/a") {
      status should be(200)
      body should include("<p>This is SSP template A")
      body should include("<p>Custom Jade footer")
    }
  }
}
