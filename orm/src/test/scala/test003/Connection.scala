package test003

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test003", "jdbc:h2:mem:test003;MODE=PostgreSQL", "sa", "sa")
}
