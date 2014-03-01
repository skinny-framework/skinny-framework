package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scalikejdbc._
import ReverseScaffoldGenerator._
import java.sql.SQLException
import grizzled.slf4j.Logging

class ReverseScaffoldGeneratorSpec extends FunSpec with ShouldMatchers with Logging {

  val tableName = "reverse_scaffold_test"
  val className = "test"

  describe("ReverseScaffoldGenerator.extractColumnsByQuery/extractColumnsByMetaData") {
    it("should be equals to expected") {
      using(getOnMemoryDatabaseConnection) { implicit conn =>
        Seq(
          (
            Seq(
              "cBoolean:Boolean",
              "cDouble:Double",
              "cDouble2:Double:number(5,2)",
              "cFloat:Float",
              "cLong:Long",
              "cInt:Int",
              "cBigDecimal:BigDecimal:number(3)",
              "cShort:Short",
              "cString:String",
              "cByte:Byte",
              "cByteArray:ByteArray",
              "cDateTime:DateTime",
              "cLocalDate:LocalDate",
              "cLocalTime:LocalTime"),
              Seq("Column(ID,-5,BIGINT,19,Some(0),Some(false))",
                "Column(C_BOOLEAN,16,BOOLEAN,1,Some(0),Some(false))",
                "Column(C_DOUBLE,8,DOUBLE,17,Some(0),Some(false))",
                "Column(C_DOUBLE2,3,DECIMAL,5,Some(2),Some(false))",
                "Column(C_FLOAT,8,DOUBLE,17,Some(0),Some(false))",
                "Column(C_LONG,-5,BIGINT,19,Some(0),Some(false))",
                "Column(C_INT,4,INTEGER,10,Some(0),Some(false))",
                "Column(C_BIG_DECIMAL,3,DECIMAL,3,Some(0),Some(false))",
                "Column(C_SHORT,4,INTEGER,10,Some(0),Some(false))",
                "Column(C_STRING,12,VARCHAR,512,Some(0),Some(false))",
                "Column(C_BYTE,-6,TINYINT,3,Some(0),Some(false))",
                "Column(C_BYTE_ARRAY,-3,VARBINARY,2147483647,Some(0),Some(false))",
                "Column(C_DATE_TIME,93,TIMESTAMP,23,Some(10),Some(false))",
                "Column(C_LOCAL_DATE,91,DATE,8,Some(0),Some(false))",
                "Column(C_LOCAL_TIME,92,TIME,6,Some(0),Some(false))").mkString(" ")),
          (
            Seq("name:String", "birthday:Option[LocalDate]"),
            Seq(
              "Column(ID,-5,BIGINT,19,Some(0),Some(false))",
              "Column(NAME,12,VARCHAR,512,Some(0),Some(false))",
              "Column(BIRTHDAY,91,DATE,8,Some(0),Some(true))").mkString(" "))).foreach { t =>
            val (args, expected) = t
            val ddl = G.migrationSQL(tableName, className, args.map(strToArg))
            try {
              executeUpdate(conn, ddl)
            } catch {
              case e: SQLException => {
                logger.info(s"DDL: ${ddl}")
                throw e
              }
            }
            try {
              val byQuery = extractColumnsByQuery(tableName)
              byQuery.mkString(" ") should equal(expected)
              val byMetaData = extractColumnsByMetaData(tableName.toUpperCase)
              byMetaData.mkString(" ") should equal(expected)
            } finally {
              executeUpdate(conn, "DROP TABLE " + tableName)
            }
          }
      }
    }
  }

  describe("SqlTypeMapping.toNameAndParamType") {

    import SqlTypeMapping._

    it("should be equals to expected") {

      using(getOnMemoryDatabaseConnection) { implicit conn =>
        Seq(
          ("target:Int", "target:Int"),
          ("target:Int:number(3)", "target:BigDecimal:number(3)"),
          ("target:Option[Int]", "target:Option[Int]"),
          ("target:String", "target:String:varchar(512)"),
          ("target:String:varchar(28)", "target:String:varchar(28)")).foreach { t =>
            val (arg, expected) = t
            val ddl = G.migrationSQL(tableName, className, Seq(strToArg(arg)))
            try {
              executeUpdate(conn, ddl)
            } catch {
              case e: SQLException => {
                logger.info(s"DDL: ${ddl}")
                throw e
              }
            }
            try {
              val column = extractColumnsByQuery(tableName).collectFirst {
                case x if x.name.equalsIgnoreCase("target") => x
              }.getOrElse(EmptyColumn)
              toScaffoldFieldDef(column) should equal(expected)
            } finally {
              logger.info(t)
              executeUpdate(conn, "DROP TABLE " + tableName)
            }
          }
      }
    }

  }

  def strToArg(str: String): ScaffoldGeneratorArg = str.split(":") match {
    case x if x.length == 3 => ScaffoldGeneratorArg(x(0), x(1), Some(x(2)))
    case x if x.length == 2 => ScaffoldGeneratorArg(x(0), x(1), None)
    case _ => fail
  }

  def getOnMemoryDatabaseConnection(): java.sql.Connection = {
    Class.forName("org.h2.Driver")
    java.sql.DriverManager.getConnection("jdbc:h2:mem:skinny", "sa", "")
  }

  def executeUpdate(conn: java.sql.Connection, sql: String): Int = {
    using(conn.createStatement) { stmt =>
      stmt.executeUpdate(sql)
    }
  }

  object G extends ScaffoldGenerator with ScaffoldSspGenerator { override def withTimestamps = false }
  object EmptyColumn extends Column("", 0, "", 0, None, None)

}
