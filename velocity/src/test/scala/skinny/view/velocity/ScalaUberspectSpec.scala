package skinny.view.velocity

import scala.collection._

import org.apache.velocity.util.introspection.{ Info, VelMethod }
import org.apache.velocity.util.introspection.UberspectImpl

import org.scalatest._

class ScalaUberspectSpec extends FlatSpec with Matchers {
  behavior of "ScalaUberspectSpec"

  val dummyInfo: Info = new Info("Dummy.scala", 5, 5)

  it should "List to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(List(1, 2, 3), dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "Map to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect
      .getIterator(Map("key1" -> "value1",
        "key2" -> "value2"), dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "mutable.ArrayBuffer to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(mutable.ArrayBuffer(1, 2, 3), dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "mutable.Map to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(mutable.Map("key1" -> "value1",
      "key2" -> "value2"), dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "Iterator to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(List(1, 2, 3).iterator, dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "JavaList to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    val list = new java.util.ArrayList[String]
    list.add("foo")
    list.add("bar")
    uberspect.getIterator(list, dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "Option(Some) to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(Some("Velocity"), dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "None to JavaIterator" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getIterator(None, dummyInfo) should be(a[java.util.Iterator[_]])
  }

  it should "List#get to VelMethodImpl" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getMethod(List(1, 2, 3),
      "get",
      Array(new Integer(1)),
      dummyInfo) should be(a[UberspectImpl.VelMethodImpl])
  }

  it should "Map#get to RewriteVelMethod" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getMethod(Map("key" -> "value"),
      "get",
      Array("key"),
      dummyInfo) should be(a[RewriteVelMethod])
  }

  it should "Map#apply to not RewriteVelMethod" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getMethod(Map("key" -> "value"),
      "apply",
      Array("key"),
      dummyInfo) should not be (a[RewriteVelMethod])
  }

  it should "mutable.ArrayBuffer#get to VelMethodImpl" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getMethod(mutable.ArrayBuffer(1, 2, 3),
      "get",
      Array(new Integer(1)),
      dummyInfo) should be(a[UberspectImpl.VelMethodImpl])
  }

  it should "mutable.Map#get to RewriteVelMethod" in {
    val uberspect = new ScalaUberspect
    uberspect.init()
    uberspect.getMethod(mutable.Map("key" -> "value"),
      "get",
      Array("key"),
      dummyInfo) should be(a[RewriteVelMethod])
  }
}
