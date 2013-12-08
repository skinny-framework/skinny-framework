package skinny.mailer

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class ScalateSkinnyMailerSupportSpec extends FlatSpec with ShouldMatchers with SkinnyMessageHelper {
  behavior of "ScalateSkinnyMailerSupport"
  val target = new ScalateSkinnyMailerSupport {}
  val templatePath = getClass.getResource("/").getPath + "test"
  val bindings = Map("name" -> "Skinny framework")

  it should "render in ssp" in {
    target.ssp(templatePath, bindings) should include("Hello Skinny framework!")
  }

  it should "render in jade" in {
    target.jade(templatePath, bindings) should include("Hello Skinny framework!")
  }

  it should "render in scaml" in {
    target.scaml(templatePath, bindings) should include("Hello Skinny framework!")
  }

  it should "render in mustache" in {
    target.mustache(templatePath, bindings) should include("Hello Skinny framework!")
  }

  it should "find XXX.html.XXX" in {
    target.ssp(templatePath, bindings, TextHtml) should include("<h1>Hello Skinny framework!</h1>")
  }
}
