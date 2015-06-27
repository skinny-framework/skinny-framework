package org.scalatra.test.scalatest

import org.junit.runner.RunWith
import org.scalatest.{ Matchers, BeforeAndAfterAll, Suite }
import org.scalatest.junit.JUnitRunner
import org.scalatra.test.ScalatraTests

/**
 * Provides Scalatra test support to ScalaTest suites.  The servlet tester
 * is started before the first test in the suite and stopped after the last.
 */
@RunWith(classOf[JUnitRunner])
trait ScalatraSuite
    extends Suite
    with ScalatraTests
    with BeforeAndAfterAll
    with Matchers {

  override protected def beforeAll(): Unit = start()

  override protected def afterAll(): Unit = stop()

}
