package skinny.validator

import org.scalatest._
import org.scalatest.matchers._

class utilsSpec extends FlatSpec with ShouldMatchers {

  behavior of "utils"

  it should "be available" in {
    val singleton = utils
    singleton should not be null
  }

  it should "have #toHasSize" in {
    utils.toHasSize(Seq(1, 2, 3)).isDefined should equal(true)
    utils.toHasSize("aaa").isDefined should equal(false)
  }

  it should "have #toHasGetTime" in {
    utils.toHasGetTime(org.joda.time.DateTime.now).isDefined should equal(true)
    utils.toHasGetTime(new java.util.Date).isDefined should equal(true)
    utils.toHasGetTime("aaa").isDefined should equal(false)
  }

}
