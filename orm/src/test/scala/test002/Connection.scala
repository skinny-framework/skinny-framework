package test002

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test002", "jdbc:h2:mem:test002;MODE=PostgreSQL", "sa", "sa")
}
