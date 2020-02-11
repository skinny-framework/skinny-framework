package service

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("service", "jdbc:h2:mem:service", "sa", "sa")
}
