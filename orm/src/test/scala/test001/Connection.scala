package test001

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("test001", "jdbc:h2:mem:test001;MODE=PostgreSQL", "sa", "sa")
}
