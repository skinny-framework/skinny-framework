package skinny.view.freemarker

import freemarker.template._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScalaObjectWrapperSpec extends AnyFlatSpec with Matchers {

  val objectWrapper = new ScalaObjectWrapper

  "ScalaObjectWrapper" should "wrap values" in {
    objectWrapper.wrap(null) should equal(TemplateModel.NOTHING)
    objectWrapper.wrap(true) should equal(TemplateBooleanModel.TRUE)

    objectWrapper.wrap(Option("foo").toString)
    objectWrapper.wrap(new java.util.Date)
    objectWrapper.wrap(123)
    objectWrapper.wrap(Seq(1, 2, 3))
  }

  "ScalaBaseWrapper" should "be available" in {
    val baseWrapper = new ScalaBaseWrapper("foo", objectWrapper)
    baseWrapper.get("xxx") should equal(TemplateModel.NOTHING)
  }

}
