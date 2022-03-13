package skinny.dbmigration

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalikejdbc.ConnectionPool

class DBMigrationSpec extends AnyFlatSpec with Matchers with DBMigration {

  Class.forName("org.h2.Driver")
  ConnectionPool.add("DBMigrationSpec", "jdbc:h2:mem:DBMigrationSpec;MODE=PostgreSQL", "sa", "sa")

  // TODO: Fix this test
//  it should "have #migrate" in {
//    migrate("migration", "DBMigrationSpec")
//  }
//
//  it should "have #repair" in {
//    repair("migration", "DBMigrationSpec")
//  }

}
