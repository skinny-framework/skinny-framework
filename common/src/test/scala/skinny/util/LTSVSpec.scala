package skinny.util

import org.scalatest._

class LTSVSpec extends FlatSpec with Matchers {

  behavior of "LTSV.parseLines"

  it should "work fine with LF" in {
    val ltsvList: List[Map[String, String]] = LTSV.parseLines("name:Alice\tage:19\nname:Bob\tage:\nname:Chris\tage:25")
    ltsvList.size should equal(3)
    LTSV.dump(ltsvList).mkString("\n") should equal("name:Alice\tage:19\nname:Bob\tage:\nname:Chris\tage:25")
  }

  it should "work fine with CRLF" in {
    val ltsvList: List[Map[String, String]] =
      LTSV.parseLines("name:Alice\tage:19\r\nname:Bobson\tage:\tnickname:Bob\nname:Chris\tage:25")
    ltsvList.size should equal(3)
    LTSV.dump(ltsvList).mkString("\n") should equal(
      "name:Alice\tage:19\nname:Bobson\tage:\tnickname:Bob\nname:Chris\tage:25"
    )
  }

  it should "throw Exception if failed" in {
    intercept[IllegalArgumentException] {
      LTSV.parseLines(":")
    }
  }

  behavior of "LTSV.parseLine"

  it should "work fine with 1 value" in {
    val ltsv: Map[String, String] = LTSV.parseLine("name:Alice")
    ltsv.size should equal(1)
    LTSV.dump(ltsv) should equal("name:Alice")
  }

  it should "work fine with 2 values" in {
    val ltsv: Map[String, String] = LTSV.parseLine("name:Alice\tage:19")
    ltsv.size should equal(2)
    LTSV.dump(ltsv) should equal("name:Alice\tage:19")
  }

  it should "throw Exception if failed" in {
    intercept[IllegalArgumentException] {
      LTSV.parseLine(":")
    }
  }

  it should "fail to parse invalid LTSV if in strict mode" in {
    intercept[IllegalArgumentException] {
      LTSV.parseLine("name:クリス\tage:28")
    }
  }

  it should "allow invalid LTSV if in lenient mode" in {
    val ltsv: Map[String, String] = LTSV.parseLine("name:クリス\tage:28", lenient = true)
    ltsv.size should equal(2)
    ltsv("name") should equal("クリス")
  }

  it should "allow invalid LTSV field name if in lenient mode" in {
    val ltsv: Map[String, String] = LTSV.parseLine("name^Cummy:Foo^Bar\tage:28", lenient = true)
    ltsv.size should equal(2)
    ltsv("name^Cummy") should equal("Foo^Bar")
  }

  behavior of "LTSV.dump"

  it should "dump the contents of a Map in some order" in {
    val string = LTSV.dump(Map("a" -> "b", "c" -> "d", "e" -> "f", "g" -> "h"))
    string should include("a:b")
    string should include("c:d")
    string should include("e:f")
    string should include("g:h")
  }

  it should "dump varargs in the order they were supplied" in {
    val string = LTSV.dump("a" -> "b", "c" -> "d", "e" -> "f", "g" -> "h")
    string should be("a:b\tc:d\te:f\tg:h")
  }

}
