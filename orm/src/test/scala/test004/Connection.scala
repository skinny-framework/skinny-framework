package test004

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test004", "jdbc:h2:mem:test004;MODE=PostgreSQL", "sa", "sa")
}
