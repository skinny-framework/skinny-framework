package skinny.controller

import org.scalatest._

class KeyAndErrorMessagesSpec extends FlatSpec with Matchers {
  behavior of "KeyAndErrorMessages"

  "hasErrors" should "be available" in {
    val messages = KeyAndErrorMessages(Map("id" -> Seq("required", "numeric")))
    messages.hasErrors("id") should equal(true)
    messages.hasErrors("name") should equal(false)
  }

  "getErrors" should "be available" in {
    val messages = KeyAndErrorMessages(Map("id" -> Seq("required", "numeric")))
    messages.getErrors("id") should equal(Seq("required", "numeric"))
    messages.getErrors("name") should equal(Nil)
  }

  "+" should "be available" in {
    val messages = KeyAndErrorMessages(Map("id" -> Seq("required", "numeric")))
    (messages + ("name" -> Seq("required"))) should equal(
      Map("id" -> Seq("required", "numeric"), "name" -> Seq("required")))
  }

  "-" should "be available" in {
    val messages = KeyAndErrorMessages(Map("id" -> Seq("required", "numeric"), "name" -> Seq("required")))
    (messages - "name") should equal(Map("id" -> Seq("required", "numeric")))
  }

  "iterator" should "be available" in {
    val messages = KeyAndErrorMessages(Map("id" -> Seq("required", "numeric"), "name" -> Seq("required")))
    (messages.iterator.foldLeft(0) { case (z, (key, seq)) => z + seq.size }) should equal(3)
  }

}
