package skinny.dbmigration

import scalikejdbc.scalatest.AutoRollback
import org.scalatest.{ fixture, Matchers }
import skinny.orm.{ Connection, CreateTables }

class DBSeedsSpec
    extends fixture.FunSpec
    with Matchers
    with Connection
    with CreateTables // just testing lock condition
    with AutoRollback {

  // see SkinnyORMSpec

}
