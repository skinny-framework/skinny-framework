package blog

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("fg", "jdbc:h2:mem:fg", "sa", "sa")
}
