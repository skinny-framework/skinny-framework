package blog

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add(Symbol("blog"), "jdbc:h2:mem:blog", "sa", "sa")
}
