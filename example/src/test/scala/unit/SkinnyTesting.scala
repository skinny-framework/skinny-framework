package unit

import skinny.test._
import skinny.logging.Logging

trait SkinnyTesting extends SkinnyTestSupport with Logging with DBSettings {
  self: org.scalatra.test.ScalatraTests =>

}

