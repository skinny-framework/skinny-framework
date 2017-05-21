package skinny.task.generator

import java.util.Locale

import scalikejdbc.NamedDB
import scalikejdbc.metadata.{ ForeignKey, Table }
import skinny.nlp.Inflector

trait ReverseGenerator extends CodeGenerator {

  def extractAssociationParams(tableName: String, pkName: Option[String], cachedTables: Seq[Table]): Seq[String] = {

    // belongsTo associations
    val foreignKeys: Seq[ForeignKey] = {
      val table: Option[Table] = {
        cachedTables
          .find((t) => t.name.toLowerCase == tableName.toLowerCase)
          .orElse(NamedDB(connectionPoolName).getTable(tableName))
      }
      table.map(_.foreignKeys.toSeq).getOrElse(Nil)
    }
    val belongsToAssociations: Seq[String] = foreignKeys.map { fk =>
      val name = toCamelCase(fk.name.toLowerCase).replace(toFirstCharUpper(fk.foreignColumnName.toLowerCase), "")
      s"${name}:Option[${toFirstCharUpper(name)}]"
    }

    // hasMany associations
    val hasManyAssociations: Seq[String] = {
      if (cachedTables.isEmpty) Nil
      else {
        val singularized   = Inflector.singularize(toClassName(tableName))
        val expectedFkName = s"${toFirstCharLower(singularized)}${pkName.map(toFirstCharUpper).getOrElse("Id")}"
        val hasManyTables: Seq[Table] = cachedTables.filter { table =>
          table.foreignKeys.exists { (fk) =>
            fk.foreignTableName.toLowerCase == tableName.toLowerCase &&
            toCamelCase(fk.name.toLowerCase) == expectedFkName
          }
        }
        hasManyTables.map { table =>
          val name = toCamelCase(Inflector.singularize(table.name.toLowerCase(Locale.ENGLISH)))
          s"${name}s:Seq[${toClassName(name)}]"
        }
      }
    }

    belongsToAssociations ++ hasManyAssociations
  }

}
