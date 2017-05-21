package unit

import skinny.micro.test.SkinnyMicroTests
import skinny.test._
import skinny.logging.Logging

trait SkinnyTesting extends SkinnyTestSupport with Logging with DBSettings { self: SkinnyMicroTests =>

}
