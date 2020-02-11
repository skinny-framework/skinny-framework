package sysadmin

import scalikejdbc.ConnectionPool

trait Connection {
  Class.forName("org.h2.Driver")
  ConnectionPool.add("sysadmin", "jdbc:h2:mem:sysadmin", "sa", "sa")
}
