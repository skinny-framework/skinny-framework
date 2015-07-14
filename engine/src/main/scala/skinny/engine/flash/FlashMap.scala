package skinny.engine.flash

import java.util.concurrent.{ ConcurrentHashMap, ConcurrentSkipListSet }
import skinny.engine.base.FlashMapSupport
import skinny.engine.data.MutableMapWithIndifferentAccess
import scala.collection.JavaConverters._

/**
 * A FlashMap is the data structure used by [[FlashMapSupport]]
 * to allow passing temporary values between sequential actions.
 *
 * FlashMap behaves like [[skinny.engine.data.MapWithIndifferentAccess]].  By
 * default, anything placed in the map is available to the current request and
 * next request, and is then discarded.
 *
 * @see FlashMapSupport
 */
class FlashMap extends MutableMapWithIndifferentAccess[Any] with Serializable {

  private[this] val m = new ConcurrentHashMap[String, Any]().asScala

  private[this] val flagged = new ConcurrentSkipListSet[String]().asScala

  /**
   * Removes an entry from the flash map.  It is no longer available for this
   * request or the next.
   */
  def -=(key: String): FlashMap.this.type = {
    m -= key
    this
  }

  /**
   * Adds an entry to the flash map.  Clears the sweep flag for the key.
   */
  def +=(kv: (String, Any)): FlashMap.this.type = {
    flagged -= kv._1
    m += kv
    this
  }

  /**
   * Creates a new iterator over the values of the flash map.  These are the
   * values that were added during the last request.
   */
  def iterator = new Iterator[(String, Any)] {
    private[this] val it = m.iterator

    def hasNext = it.hasNext

    def next = {
      val kv = it.next
      flagged += kv._1
      kv
    }
  }

  /**
   * Returns the value associated with a key and flags it to be swept.
   */
  def get(key: String): Option[Any] = {
    flagged += key
    m.get(key)
  }

  /**
   * Removes all flagged entries.
   */
  def sweep(): Unit = {
    flagged foreach { key => m -= key }
  }

  /**
   * Clears all flags so no entries are removed on the next sweep.
   */
  def keep(): Unit = {
    flagged.clear()
  }

  /**
   * Clears the flag for the specified key so its entry is not removed on the next sweep.
   */
  def keep(key: String): Unit = {
    flagged -= key
  }

  /**
   * Flags all current keys so the entire map is cleared on the next sweep.
   */
  def flag(): Unit = {
    flagged ++= m.keys
  }

  /**
   * Sets a value for the current request only.  It will be removed before the next request unless explicitly kept.
   * Data put in this object is availble as usual:
   * {{{
   * flash.now("notice") = "logged in succesfully"
   * flash("notice") // "logged in succesfully"
   * }}}
   */
  object now {

    def update(key: String, value: Any) = {
      flagged += key
      m += key -> value
    }
  }

}