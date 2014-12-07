package skinny

import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.collection.mutable.HashMap

class SkinnySpec extends ScalatraFlatSpec {
  behavior of "Skinny"

  val requestScope = new HashMap[String, Any]
  requestScope.put("contextPath", "/foo")
  requestScope.put("params", Params(Map("foo" -> "bar")))
  requestScope.put("multiParams", MultiParams(Map("foo" -> Seq("bar"))))

  val i18n = I18n()
  requestScope.put("i18n", i18n)

  val skinnyObject = Skinny(requestScope)

  it should "have #contextPath" in {
    skinnyObject.contextPath should equal("/foo")
  }

  it should "have #env" in {
    System.setProperty(SkinnyEnv.PropertyKey, "test")
    skinnyObject.env should equal("test")
    skinnyObject.getEnv should equal("test")
  }

  it should "have #params" in {
    skinnyObject.params should equal(Params(Map("foo" -> "bar")))
    skinnyObject.getParams should equal(Params(Map("foo" -> "bar")))
  }

  it should "have #multiParams" in {
    skinnyObject.multiParams should equal(MultiParams(Map("foo" -> Seq("bar"))))
    skinnyObject.getMultiParams should equal(MultiParams(Map("foo" -> Seq("bar"))))
  }

  it should "have #csrfHiddenInputTag" in {
    val expected = "<input type=\"hidden\" name=\"null\" value=\"null\"/>"
    skinnyObject.csrfHiddenInputTag should equal(expected)
    skinnyObject.getCsrfHiddenInputTag should equal(expected)
  }

  it should "have #i18n" in {
    skinnyObject.i18n should equal(i18n)
    skinnyObject.getI18n should equal(i18n)
  }

  object Controller extends SkinnyController with Routes {
    val indexUrl = get("/")("ok").as('index)
    get("/redirect") {
      val urlString = skinny.Skinny(requestScope(request)).url(indexUrl, "id" -> "123")
      urlString should equal("/?id=123")
      redirect302(url(indexUrl))
    }
  }
  addFilter(Controller, "/*")

  it should "have #url" in {
    get("/redirect") {
      status should equal(302)
    }
  }

}
