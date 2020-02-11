package skinny

import javax.servlet.http.HttpServletRequest
import skinny.micro.UnstableAccessValidation
import skinny.micro.context.SkinnyContext
import org.scalatra.test.scalatest.ScalatraFlatSpec
import scala.collection.mutable.HashMap
import org.scalatestplus.mockito.MockitoSugar

class SkinnySpec extends ScalatraFlatSpec with MockitoSugar {
  behavior of "Skinny"

  val requestScope = new HashMap[String, Any]
  val request      = mock[HttpServletRequest]

  requestScope.put("contextPath", "/foo")
  requestScope.put("params", Params(Map("foo"           -> "bar")))
  requestScope.put("multiParams", MultiParams(Map("foo" -> Seq("bar"))))

  val i18n = I18n()
  requestScope.put("i18n", i18n)

  val skinnyObject =
    Skinny(SkinnyContext.buildWithRequest(request, UnstableAccessValidation(true, false)), requestScope)

  it should "have #contextPath" in {
    skinnyObject.contextPath should equal("/foo")
    skinnyObject.getContextPath should equal("/foo")
  }

  it should "have #env" in {
    System.setProperty(SkinnyEnv.PropertyKey, "test")
    skinnyObject.env should equal("test")
    skinnyObject.getEnv should equal("test")
  }

  it should "have #params" in {
    skinnyObject.params should equal(Params(Map("foo"    -> "bar")))
    skinnyObject.getParams should equal(Params(Map("foo" -> "bar")))
  }

  it should "have #multiParams" in {
    skinnyObject.multiParams should equal(MultiParams(Map("foo"    -> Seq("bar"))))
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

  it should "have #errorMessages" in {
    skinnyObject.errorMessages.size should equal(0)
    skinnyObject.getErrorMessages.size should equal(0)
  }

  it should "have #keyAndErrorMessages" in {
    skinnyObject.keyAndErrorMessages.size should equal(0)
    skinnyObject.getKeyAndErrorMessages.size should equal(0)
  }

  it should "have #requestPath" in {
    skinnyObject.requestPath should equal(null)
    skinnyObject.getRequestPath should equal(null)
  }

  it should "have #requestPathWithQueryString" in {
    skinnyObject.requestPathWithQueryString should equal(null)
    skinnyObject.getRequestPathWithQueryString should equal(null)
  }

  it should "have #flash" in {
    skinnyObject.flash should equal(null)
    skinnyObject.getFlash should equal(null)
  }

  it should "have #csrfKey" in {
    skinnyObject.csrfKey should equal(null)
    skinnyObject.getCsrfKey should equal(null)
  }

  it should "have #csrfToken" in {
    skinnyObject.csrfToken should equal(null)
    skinnyObject.getCsrfToken should equal(null)
  }

  it should "have #csrfMetaTag" in {
    val expected = """<meta content="null" name="null" />"""
    skinnyObject.csrfMetaTag should equal(expected)
    skinnyObject.csrfMetaTags should equal(expected)
    skinnyObject.getCsrfMetaTag should equal(expected)
  }

  it should "have #getAs, #set" in {
    skinnyObject.getAs[String]("foo") should equal(None)
    skinnyObject.set("foo", "bar")
    skinnyObject.getAs[String]("foo") should equal(Some("bar"))
  }

  object Controller extends SkinnyController with Routes {
    val indexUrl = get("/")("ok").as("index")
    get("/redirect") {
      val skinnyObject =
        Skinny(SkinnyContext.buildWithRequest(request, UnstableAccessValidation(true, false)), requestScope)
      val urlString = skinnyObject.url(indexUrl, "id" -> "123")
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
