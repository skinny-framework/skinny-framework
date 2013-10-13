package skinny.orm

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:mem:skinny-mapper-test", "sa", "sa")
}
