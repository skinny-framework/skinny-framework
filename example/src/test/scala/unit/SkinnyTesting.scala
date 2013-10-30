package unit

import skinny.test._
import org.scalatest._
import grizzled.slf4j.Logging

trait SkinnyTesting
    extends SkinnyTestSupport
    with Logging { self: org.scalatra.test.ScalatraTests =>

  skinny.bootstrap.DBSettings.initialize()
  lib.DBInitializer.synchronized {
    lib.DBInitializer.initialize()
  }

}

