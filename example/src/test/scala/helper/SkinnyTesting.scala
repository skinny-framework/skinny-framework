package helper

import skinny.test._
import grizzled.slf4j.Logging

trait SkinnyTesting
    extends SkinnyTestSupport
    with Logging
    with DBSettings
    with DBMigration { self: org.scalatra.test.ScalatraTests =>

}

