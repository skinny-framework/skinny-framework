package skinny.task.generator

import skinny._
import scalikejdbc._
import java.sql.{
  Connection,
  Statement,
  ResultSet,
  SQLException,
  DatabaseMetaData,
  ResultSetMetaData
}

/**
 * Skinny Reverse Generator Task.
 */
object ReverseScaffoldGenerator extends ReverseScaffoldGenerator

/**
 * Skinny Reverse Generator Task.
 */
trait ReverseScaffoldGenerator extends CodeGenerator {

  private def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-scaffold table_name Class" """)
    println("")
  }

  private def showErrors(messages: Seq[String]) = {
    showSkinnyGenerator()
    println("""  Command failed!""")
    println("")
    println(messages.mkString("  Error: ", "\n", "\n"))
  }

  override protected def showSkinnyGenerator(): Unit = {
    println("""
 *** Skinny Reverse Generator Task ***
""")
  }

  def run(templateType: String, args: List[String]): Unit = {
    if (args.size < 2) {
      showUsage
      return
    }
    val tableName = args(0)
    val className = args(1)
    val env = if (args.size >= 3) args(2) else "development"
    showSkinnyGenerator
    val columnInfos = extractColumnInfos(env, tableName)
    val nameAndParamTypes = columnInfos.map(SqlTypeMapping.toNameAndParamType)
    val params: List[String] = List(tableName, className) ++ nameAndParamTypes.filter {
      // filter the parameters not to pass to ScaffoldGenerator
      case "id:Long" | "id:Option[Long]" => false
      case _ => true
    }
    println()
    println("  params:")
    params.foreach(x => println("    " + x))
    println()
    val g = templateType match {
      case "ssp" => new ScaffoldSspGenerator { override def withTimestamps: Boolean = false }
      case "scaml" => new ScaffoldScamlGenerator { override def withTimestamps: Boolean = false }
      case "jade" => new ScaffoldJadeGenerator { override def withTimestamps: Boolean = false }
      case x => throw new IllegalArgumentException("unknown template type: " + x)
    }
    g.run(params)
  }

  def extractColumnInfos(targetEnv: String, tableName: String): ColumnInfos =
    using(getConnection(targetEnv)) { implicit conn =>
      Seq(
        () => extractColumnInfosByQuery(tableName),
        () => extractColumnInfosByMetaData(tableName),
        () => extractColumnInfosByMetaData(tableName.toLowerCase),
        () => extractColumnInfosByMetaData(tableName.toUpperCase)).foreach { f =>
          try {
            val result = f()
            if (!result.isEmpty) return result
          } catch {
            case e: SQLException => println("  warn: " + e)
            case e: Throwable => throw e
          }
        }
      Nil
    }

  def extractColumnInfosByQuery(tableName: String)(implicit conn: Connection): ColumnInfos =
    using(conn.createStatement) { stmt =>
      val sql = s"SELECT * FROM ${tableName} LIMIT 0"
      val m = stmt.executeQuery(sql).getMetaData
      (1 to m.getColumnCount).toList.map { i =>
        val decimalDigitsOpt = Some(m.getScale(i))
        val nullableOpt = toOptionalNullable(m.isNullable(i))
        ColumnInfo(m.getColumnName(i), m.getColumnType(i),
          m.getColumnTypeName(i), m.getPrecision(i), decimalDigitsOpt, nullableOpt)
      }
    }

  def extractColumnInfosByMetaData(tableName: String)(implicit conn: Connection): ColumnInfos =
    using(conn.getMetaData.getColumns(null, null, tableName, null)) { rs =>
      def f(infos: ColumnInfos): ColumnInfos =
        if (rs.next) {
          val decimalDigitsOpt = rs.getInt(9) match {
            case _ if rs.wasNull => None
            case x => Some(x)
          }
          val nullableOpt = rs.getInt(11) match {
            case _ if rs.wasNull => None
            case x => toOptionalNullable(x)
          }
          f(ColumnInfo(
            rs.getString(4), rs.getInt(5), rs.getString(6),
            rs.getInt(7), decimalDigitsOpt, nullableOpt) :: infos)
        } else infos
      return f(Nil).reverse
    }

  def toOptionalNullable(v: Int): Option[Boolean] = v match {
    case 0 /* attributeNoNulls */ => Some(false)
    case 1 /* attributeNullable */ => Some(true)
    case 2 /* attributeNullableUnknown */ => None
    case _ => None
  }

  def getConnection(env: String): Connection = {
    val x = scalikejdbc.config.TypesafeConfigReaderWithEnv(env).readJDBCSettings()
    CommonsConnectionPoolFactory(x.url, x.user, x.password).borrow
  }

  /**
   * A structure of database column info.
   */
  case class ColumnInfo(
    name: String, dataType: Int, typeName: String, size: Int,
    decimalDigits: Option[Int], nullable: Option[Boolean])

  type ColumnInfos = List[ColumnInfo]

  /**
   * SQL type mapping utility.
   */
  object SqlTypeMapping {

    import skinny.ParamType._
    import java.sql.Types._

    def toNameAndParamType(info: ColumnInfo): String = {
      val optionableParamType = toOptionableParamType(info)
      val paramType = optionableParamType match {
        case Some(x) => x
        case x => x
      }
      val paramTypeName = paramType.getClass.getSimpleName.replace("$", "")
      val paramTypeString = (paramTypeName, optionableParamType) match {
        case (x, Some(_)) => s"Option[$x]"
        case (x, _) => x
      }
      val sqlTypeOpt: Option[String] = (paramType, info) match {
        case (String | Some(String), x) => Option("varchar(" + x.size + ")")
        case (BigDecimal, x) => Option("NUMBER(" + x.size + ")")
        case _ => None
      }
      val sqlTypeString = sqlTypeOpt match {
        case Some(x) => ":" + x
        case _ => ""
      }
      info.name.toLowerCase + ":" + paramTypeString + sqlTypeString
    }

    def toOptionableParamType(info: ColumnInfo): Any = {
      // add custom mappings if necessary
      jdbcSqlTypeToParamType(info.dataType) match {
        case x if info.nullable.getOrElse(true) => Option(x)
        case x => x
      }
    }

    def jdbcSqlTypeToParamType(dataType: Int): Any = dataType match {
      case CHAR | VARCHAR | LONGVARCHAR | LONGNVARCHAR | NCHAR | NVARCHAR | CLOB | NCLOB => String
      case BOOLEAN | BIT => Boolean
      case TINYINT => Byte
      case SMALLINT => Short
      case INTEGER => Int
      case BIGINT => Long
      case REAL => Float
      case DOUBLE | FLOAT => Double
      case DECIMAL | NUMERIC => BigDecimal
      case DATE => LocalDate
      case TIME => LocalTime
      case TIMESTAMP => DateTime
      case BINARY | VARBINARY | LONGVARBINARY | BLOB => ByteArray
      case ARRAY => "Array"
      case DATALINK => "DataLink"
      case DISTINCT => "Distinct"
      case JAVA_OBJECT => "JavaObject"
      case NULL => "Null"
      case OTHER => "Other"
      case REF => "Ref"
      case ROWID => "RowId"
      case STRUCT => "Struct"
    }

  }

}
