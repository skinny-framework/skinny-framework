package skinny.dbmigration

import scalikejdbc.scalatest.AutoRollback
import org.scalatest.{ Matchers, fixture }
import skinny.orm.{ CreateTables, Connection }

class DBSeedsSpec extends fixture.FunSpec with Matchers
    with Connection
    with CreateTables // just testing lock condition
    with AutoRollback {

  // see SkinnyORMSpec

}