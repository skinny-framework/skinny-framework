package skinny.task.generator

import skinny._
import scalikejdbc._
import java.sql.{ ResultSet, Connection, SQLException }
import java.util.Locale

object ReverseScaffoldGenerator extends ReverseScaffoldGenerator

/**
 * Skinny Reverse Generator Task.
 */
trait ReverseScaffoldGenerator extends CodeGenerator {

  case class Column(
    name: String,
    dataType: Int,
    typeName: String,
    size: Int,
    decimalDigits: Option[Int],
    nullable: Option[Boolean])

  case class ParamTypeAndNullable(
    paramType: ParamType,
    nullable: Boolean)

  protected def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:reverse-scaffold table_name resources resource" """)
    println()
  }

  protected override def showSkinnyGenerator() = {
    println("""
 *** Skinny Reverse Engineering Task ***
""")
  }

  def run(templateType: String, args: List[String]): Unit = {
    if (args.size < 3) {
      showUsage
      return
    }
    try {
      val (tableName, resources, resource) = (args(0), args(1), args(2))

      val skinnyEnv = if (args.size >= 4) args(3) else SkinnyEnv.Development
      val fields = extractColumns(skinnyEnv, tableName)
        .map(column => SqlTypeMapping.toScaffoldFieldDef(column))
        .filter(param => param != "id:Long" && param != "id:Option[Long]")
        .map(param => toCamelCase(param))

      println(s"""
              | *** Skinny Reverse Engineering Task ***
              |
              |  Table: ${tableName}
              |  Resources: ${resources}
              |  Resource: ${resource}
              |
              |  Columns:
              |${fields.map(f => s"   - ${f}").mkString("\n")}""".stripMargin)

      val generator = templateType match {
        case "ssp" => new ScaffoldSspGenerator {
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
        }
        case "scaml" => new ScaffoldScamlGenerator {
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
        }
        case "jade" => new ScaffoldJadeGenerator {
          override def withTimestamps: Boolean = false
          override def skipDBMigration = true
        }
        case _ => throw new IllegalArgumentException("Unknown template type: " + templateType)
      }
      generator.run(Seq(resources, resource) ++ fields)

    } catch {
      case e: Exception =>
        showErrors(Seq(e.getMessage))
    }
  }

  def extractColumns(targetEnv: String, tableName: String): Seq[Column] = {
    using(borrowConnection(targetEnv)) { implicit conn =>
      val extractors = Seq(
        () => extractColumnsByQuery(tableName),
        () => extractColumnsByMetaData(tableName),
        () => extractColumnsByMetaData(tableName.toLowerCase),
        () => extractColumnsByMetaData(tableName.toUpperCase)
      )
      extractors.foldLeft[Option[Seq[Column]]](None) {
        case (z, extractor) =>
          try {
            z.orElse {
              val columns = extractor.apply()
              if (columns.isEmpty) z else Some(columns)
            }
          } catch {
            case e: SQLException => z
            case e: Throwable => throw e
          }
      }.getOrElse {
        throw new IllegalStateException(s"Failed to retrieve meta data about columns for ${tableName}")
      }
    }
  }

  def extractColumnsByQuery(tableName: String)(implicit conn: Connection): Seq[Column] = {
    using(conn.createStatement) { stmt =>
      val meta = stmt.executeQuery(s"SELECT * FROM ${tableName} LIMIT 0").getMetaData
      (1 to meta.getColumnCount).map { i =>
        Column(
          name = meta.getColumnName(i),
          dataType = meta.getColumnType(i),
          typeName = meta.getColumnTypeName(i),
          size = meta.getPrecision(i),
          decimalDigits = Option(meta.getScale(i)),
          nullable = extractNullable(meta.isNullable(i))
        )
      }
    }
  }

  def extractColumnsByMetaData(tableName: String)(implicit conn: Connection): Seq[Column] =
    using(conn.getMetaData.getColumns(null, null, tableName, null)) { rs =>
      def extract(rs: ResultSet, columns: Seq[Column]): Seq[Column] = {
        if (rs.next) {
          extract(rs, {
            columns :+ Column(
              name = rs.getString(4),
              dataType = rs.getInt(5),
              typeName = rs.getString(6),
              size = rs.getInt(7),
              decimalDigits = rs.getInt(9) match {
                case _ if rs.wasNull => None
                case digits => Some(digits)
              },
              nullable = rs.getInt(11) match {
                case _ if rs.wasNull => None
                case nullable => extractNullable(nullable)
              })
          })
        } else columns
      }
      extract(rs, Nil)
    }

  // None: unknown
  protected def extractNullable(v: Int): Option[Boolean] = v match {
    case 0 /* attributeNoNulls */ => Some(false)
    case 1 /* attributeNullable */ => Some(true)
    case 2 /* attributeNullableUnknown */ => None
    case _ => None
  }

  protected def borrowConnection(env: String): Connection = {
    val jdbc = scalikejdbc.config.TypesafeConfigReaderWithEnv(env).readJDBCSettings()
    CommonsConnectionPoolFactory(jdbc.url, jdbc.user, jdbc.password).borrow
  }

  /**
   * SQL type mapping utility.
   */
  object SqlTypeMapping {

    import skinny.ParamType._
    import java.sql.Types._

    def toScaffoldFieldDef(column: Column): String = {
      val ParamTypeAndNullable(paramType, nullable) = toParamTypeAndNullable(column)
      val paramTypeString: String = if (nullable) s"Option[$paramType]" else paramType.toString
      val columnTypeString: String = {
        paramType match {
          case String => s":varchar(${column.size})"
          case BigDecimal => s":number(${column.size})"
          case _ => ""
        }
      }
      toCamelCase(column.name.toLowerCase(Locale.ENGLISH)) + ":" + paramTypeString + columnTypeString
    }

    def toParamTypeAndNullable(column: Column): ParamTypeAndNullable = {
      ParamTypeAndNullable(
        paramType = convertJdbcSqlTypeToParamType(column.dataType),
        nullable = column.nullable.getOrElse(true)
      )
    }

    def convertJdbcSqlTypeToParamType(dataType: Int): ParamType = dataType match {
      case CHAR | VARCHAR | LONGVARCHAR | LONGNVARCHAR | NCHAR | NVARCHAR | CLOB | NCLOB => String
      case BOOLEAN | BIT => Boolean
      case TINYINT => Byte
      case SMALLINT => Short
      case INTEGER => Int
      case BIGINT => Long
      case FLOAT | REAL => Float
      case DOUBLE => Double
      case NUMERIC | DECIMAL => BigDecimal
      case DATE => LocalDate
      case TIME => LocalTime
      case TIMESTAMP => DateTime
      case BINARY | VARBINARY | LONGVARBINARY | BLOB => ByteArray
      case _ => String
    }

  }

}
