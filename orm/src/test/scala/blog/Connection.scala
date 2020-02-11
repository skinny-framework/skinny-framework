package blog

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("blog", "jdbc:h2:mem:blog", "sa", "sa")
}
