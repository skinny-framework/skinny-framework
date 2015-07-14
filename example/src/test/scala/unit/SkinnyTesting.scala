package unit

import skinny.engine.test.SkinnyEngineTests
import skinny.test._
import skinny.logging.Logging

trait SkinnyTesting extends SkinnyTestSupport with Logging with DBSettings {
  self: SkinnyEngineTests =>

}

