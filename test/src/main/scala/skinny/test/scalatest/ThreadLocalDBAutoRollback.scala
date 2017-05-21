package skinny.test.scalatest

import org.scalatest.{ BeforeAndAfter, Suite }
import scalikejdbc.{ ConnectionPool, ThreadLocalDB }

/**
  * ThreadLocalDB auto rollback SUPPORT
  */
trait ThreadLocalDBAutoRollback extends BeforeAndAfter { self: Suite =>

  before {
    Option(ThreadLocalDB.load())
      .getOrElse {
        ThreadLocalDB.create(ConnectionPool.borrow())
      }
      .beginIfNotYet()
  }

  after {
    ThreadLocalDB.load().rollbackIfActive()
  }

}
