package skinny.dbmigration

import org.scalatest._
import scalikejdbc.ConnectionPool

class DBMigrationSpec extends FlatSpec with Matchers with DBMigration {

  Class.forName("org.h2.Driver")
  ConnectionPool.add("DBMigrationSpec", "jdbc:h2:mem:DBMigrationSpec;MODE=PostgreSQL", "sa", "sa")

  it should "have #migrate" in {
    migrate("migration", "DBMigrationSpec")
  }

  it should "have #repair" in {
    repair("migration", "DBMigrationSpec")
  }

}
