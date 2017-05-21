package skinny.mailer.test

import org.jvnet.mock_javamail.Mailbox
import javax.mail._

/**
  * Enriched Mailbox for an email address.
  */
trait RichMailBox {

  /**
    * Mailbox
    */
  def underlying: Mailbox

  /**
    * Clears all the received message from this mailbox.
    */
  def clearReceivedMessages = underlying.clear()

  /**
    * Returns received messages.
    */
  def receivedMessages: List[Message] = {
    for (i <- 0 until underlying.size) yield underlying.get(i)
  }.toList

  /**
    * Returns count of received messages.
    */
  def size: Int = underlying.size

  /**
    * Returns count of new messages.
    */
  def newMessageCount: Int = underlying.getNewMessageCount

  /**
    * Returns true if this mailbox is flagged as 'error'.
    */
  def isError: Boolean = underlying.isError

  def error_=(error: Boolean): Unit = underlying.setError(error)

  /**
    * Returns true if received messages are empty.
    */
  def isEmpty: Boolean = underlying.isEmpty

}
