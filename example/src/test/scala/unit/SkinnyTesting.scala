package unit

import skinny.test._
import org.scalatest._
import skinny.logging.Logging
import scalikejdbc.{ LoggingSQLAndTimeSettings, GlobalSettings }

trait SkinnyTesting extends SkinnyTestSupport with Logging {
  self: org.scalatra.test.ScalatraTests =>

  skinny.DBSettings.initialize()
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(singleLineMode = true)
  lib.DBInitializer.synchronized { lib.DBInitializer.initialize() }

}

