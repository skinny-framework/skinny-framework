package whitespace

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add(Symbol("ws"), "jdbc:h2:mem:ws", "sa", "sa")
}
