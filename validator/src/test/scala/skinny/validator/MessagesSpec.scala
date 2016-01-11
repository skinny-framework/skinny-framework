package skinny.validator

import org.scalatest._

class MessagesSpec extends FlatSpec with Matchers {

  behavior of "Messages"

  it should "load from properties" in {
    val msgs = Messages.loadFromProperties()
    msgs.get("foo").get should equal("bar")
    msgs.get("fooo").isDefined should equal(false)
    msgs.get("intMinValue", Seq("age", 20)).get should equal(
      "age should be greater than or equal to 20.")

    val messages = Validator(
      param("age" -> 12) is intMinValue(20)
    ).failure { (inputs, errors) =>
        inputs.keys.flatMap { key =>
          errors.get(key).map(error => msgs.get(error.name, key :: error.messageParams.toList).get)
        }
      }.apply()

    messages.size should equal(1)
  }

  it should "load from messages.conf" in {
    val msgs = Messages.loadFromConfig()
    msgs.get("foo").get should equal("bar")
    msgs.get("fooo").isDefined should equal(false)
    msgs.get("intMinValue", Seq("age", 20)).get should equal(
      "age should be greater than or equal to 20.（日本語）")

    val messages = Validator(
      param("age" -> 12) is intMinValue(20)
    ).failure { (inputs, errors) =>
        inputs.keys.flatMap { key =>
          errors.get(key).map(error => msgs.get(error.name, key :: error.messageParams.toList).get)
        }
      }.apply()

    messages.size should equal(1)
  }

  it should "load user.name correctly" in {
    val msgs = Messages.loadFromConfig()
    msgs.get("user.name") should equal(None)
  }

}
