package blog2

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("blog2", "jdbc:h2:mem:blog2", "sa", "sa")
}
