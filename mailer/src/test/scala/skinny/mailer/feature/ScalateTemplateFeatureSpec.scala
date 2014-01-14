package skinny.mailer.feature

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.mailer.implicits.SkinnyMailerImplicits
import skinny.mailer.Html

class ScalateTemplateFeatureSpec extends FlatSpec with ShouldMatchers with SkinnyMailerImplicits {

  behavior of "ScalateTemplateFeature"

  val target = new ScalateTemplateFeature {}
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
    target.ssp(templatePath, bindings, Html) should include("<h1>Hello Skinny framework!</h1>")
  }

}
