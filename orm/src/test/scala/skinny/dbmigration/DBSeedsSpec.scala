package skinny.dbmigration

import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture
import org.scalatest.matchers.ShouldMatchers
import skinny.orm.{ CreateTables, Connection }

class DBSeedsSpec extends fixture.FunSpec with ShouldMatchers
    with Connection
    with CreateTables // just testing lock condition
    with AutoRollback {

  // see SkinnyORMSpec

}