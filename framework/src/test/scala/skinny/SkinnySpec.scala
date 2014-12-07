package skinny

import org.scalatest._

import scala.collection.mutable.HashMap

class SkinnySpec extends FlatSpec with Matchers {
  behavior of "Skinny"

  val requestScope = new HashMap[String, Any]
  requestScope.put("contextPath", "/foo")
  requestScope.put("params", Params(Map("foo" -> "bar")))

  val skinnyObject = Skinny(requestScope)

  it should "work" in {
    skinnyObject.contextPath should equal("/foo")

    System.setProperty(SkinnyEnv.PropertyKey, "test")
    skinnyObject.env should equal("test")
    skinnyObject.getEnv should equal("test")

    skinnyObject.params should equal(Params(Map("foo" -> "bar")))
    skinnyObject.getParams should equal(Params(Map("foo" -> "bar")))
  }

}
