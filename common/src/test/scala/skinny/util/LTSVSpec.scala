package skinny.util

import org.scalatest._
import org.scalatest.matchers._

class LTSVSpec extends FlatSpec with ShouldMatchers {

  behavior of "LTSV.parseLines"

  it should "work fine with LF" in {
    val ltsvList: List[Map[String, String]] = LTSV.parseLines("name:Alice\tage:19\nname:Bob\tage:\nname:Chris\tage:25")
    ltsvList.size should equal(3)
    LTSV.dump(ltsvList).mkString("\n") should equal("name:Alice\tage:19\nname:Bob\tage:\nname:Chris\tage:25")
  }

  it should "work fine with CRLF" in {
    val ltsvList: List[Map[String, String]] = LTSV.parseLines("name:Alice\tage:19\r\nname:Bobson\tage:\tnickname:Bob\nname:Chris\tage:25")
    ltsvList.size should equal(3)
    LTSV.dump(ltsvList).mkString("\n") should equal("name:Alice\tage:19\nname:Bobson\tage:\tnickname:Bob\nname:Chris\tage:25")
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

}