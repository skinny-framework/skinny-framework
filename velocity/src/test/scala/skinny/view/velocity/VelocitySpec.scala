package skinny.view.velocity

import scala.collection._

import org.scalatest._

class VelocitySpec extends FlatSpec with Matchers with VelocityTestSupport {
  behavior of "VelocitySpec"

  it should "#foreach List" in {
    val template =
      """|#foreach ($e in $list)
         |$e
         |#end""".stripMargin

    eval(template, "list" -> List(1, 2, 3)) should include(
      """|1
         |2
         |3""".stripMargin
    )
  }

  it should "#foreach Option(Some)" in {
    val template =
      """|#foreach ($e in $option)
         |$e
         |#end""".stripMargin

    eval(template, "option" -> Some("foo")) should include("foo")
  }

  it should "#foreach Option(None)" in {
    val template =
      """|#foreach ($e in $option)
         |$e
         |#end""".stripMargin

    eval(template, "option" -> None).trim should equal("")
  }

  it should "#foreach Map" in {
    val template =
      """|#foreach ($v in $map)
         |$v
         |#end""".stripMargin

    eval(template, "map" -> Map("key1" -> "value1", "key2" -> "value2")) should include(
      """|value1
         |value2""".stripMargin
    )
  }

  it should "#foreach JavaList" in {
    val template =
      """|#foreach ($e in $list)
         |$e
         |#end""".stripMargin

    eval(template, "list" -> java.util.Arrays.asList(1, 2, 3)) should include(
      """|1
         |2
         |3""".stripMargin
    )
  }

  it should "access property val" in {
    class Foo(val name: String)
    val template = "$foo.name"
    eval(template, "foo" -> new Foo("bar")) should equal("bar")
  }

  it should "access property var" in {
    class Foo(var name: String)
    val template = "$foo.name"
    eval(template, "foo" -> new Foo("bar")) should equal("bar")
  }

  it should "access List.get(n), get rewrite to apply" in {
    val template = "$list.get(0)"
    eval(template, "list" -> List("one", "two", "three")) should equal("one")
  }

  it should "access List[n], get rewrite to apply" in {
    val template = "$list[0]"
    eval(template, "list" -> List("one", "two", "three")) should equal("one")
  }

  it should "access mutable.ArrayBuffer.get(n), get rewrite to apply" in {
    val template = "$array.get(0)"
    eval(template, "array" -> mutable.ArrayBuffer("one", "two", "three")) should equal("one")
  }

  it should "access mutable.ArrayBuffer[n], get rewrite to apply" in {
    val template = "$array[0]"
    eval(template, "array" -> mutable.ArrayBuffer("one", "two", "three")) should equal("one")
  }

  it should "access Map['key'], get rewrite to getOrElse" in {
    val template = "$map['key']"
    eval(template, "map" -> Map("key" -> "value")) should equal("value")
  }

  it should "access Map.key, get rewrite to getOrElse" in {
    val template = "$map.key"
    eval(template, "map" -> Map("key" -> "value")) should equal("value")
  }

  it should "access Map.missing, get rewrite to getOrElse" in {
    val template = "$map.missing"
    eval(template, "map" -> Map("key" -> "value")) should equal("$map.missing")
  }

  it should "access Map.keys, method call" in {
    val template = "$map.keys"
    eval(template, "map" -> Map("key1" -> "value1", "key2" -> "value2")) should include("key1, key2")
  }

  it should "access Map.keys(), method call" in {
    val template = "$map.keys()"
    eval(template, "map" -> Map("key1" -> "value1", "key2" -> "value2")) should include("key1, key2")
  }

  it should "access Option(Some).get, get rewrite to getOrElse(null)" in {
    val template = "$option.get"
    eval(template, "option" -> Some("foo")) should equal("foo")
  }

  it should "access Option(Some).get(), get rewrite to getOrElse(null)" in {
    val template = "$option.get()"
    eval(template, "option" -> Some("foo")) should equal("foo")
  }

  it should "access Option(None).get, get rewrite to getOrElse(null)" in {
    val template = "$option.get"
    eval(template, "option" -> None) should equal("$option.get")
  }

  it should "access Option(None).get(), get rewrite to getOrElse(null)" in {
    val template = "$option.get()"
    eval(template, "option" -> None) should equal("$option.get()")
  }

  it should "method call, no argument" in {
    class Foo {
      def say: String = "bar"
    }
    val template = "$foo.say"
    eval(template, "foo" -> new Foo) should equal("bar")
  }

  it should "method call with parentheses, no argument" in {
    class Foo {
      def say: String = "bar"
    }
    val template = "$foo.say()"
    eval(template, "foo" -> new Foo) should equal("bar")
  }

  it should "method call, with argument" in {
    class Foo {
      def say(n: String): String = s"bar $n"
    }
    val template = "$foo.say('fuga')"
    eval(template, "foo" -> new Foo) should equal("bar fuga")
  }

}
