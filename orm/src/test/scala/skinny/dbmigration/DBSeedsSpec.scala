package skinny.dbmigration

import org.scalatest.funspec.FixtureAnyFunSpec
import org.scalatest.matchers.should.Matchers
import scalikejdbc.scalatest.AutoRollback
import skinny.orm.{ Connection, CreateTables }

class DBSeedsSpec
    extends FixtureAnyFunSpec
    with Matchers
    with Connection
    with CreateTables // just testing lock condition
    with AutoRollback {

  // see SkinnyORMSpec

}
