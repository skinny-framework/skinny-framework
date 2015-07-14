package skinny.controller

import org.scalatra.test.scalatest._
import skinny.engine.flash.FlashMap

class FlashSpec extends ScalatraFlatSpec {

  behavior of "Flash"

  it should "wrap scalatra's flash" in {
    val scalatraFlash = new FlashMap()
    scalatraFlash += ("name" -> "skinny")
    scalatraFlash += ("lang" -> Option("scala"))
    scalatraFlash += ("nil" -> Option(None))
    val flash = new Flash(scalatraFlash)

    flash.get("name") should be(Some("skinny"))
    flash.name should be(Some("skinny"))
    flash.get("namae") should be(None)
    flash.namae should be(None)

    flash.get("lung") should be(None)
    flash.lung should be(None)

    // issue #38 flash.get("notice") and flash.notice are inconsistent
    flash.get("lang") should be(Some(Some("scala")))
    flash.lang should be(Some(Some("scala")))

    flash.get("nil") should be(Some(Some(None)))
    flash.nil should be(Some(Some(None)))
  }

}
