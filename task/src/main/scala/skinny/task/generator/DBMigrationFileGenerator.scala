package skinny.task.generator

import java.io.File
import org.joda.time.DateTime

/**
 * DB migration file generator.
 */
object DBMigrationFileGenerator extends DBMigrationFileGenerator

trait DBMigrationFileGenerator extends CodeGenerator {

  private[this] def showUsage = {
    println("""
 *** Skinny Generator Task ***

  Usage: sbt "task/run generate:migration Create_members_table 'create table members(id bigserial not null primary key, name varchar(255) not null);'"
""")
  }

  def run(args: List[String]) {
    args.toList match {
      case name :: sqlParts =>
        println("""
 *** Skinny Generator Task ***
""")
        generate(name, sqlParts.map(_.replaceFirst("^'", "").replaceFirst("'$", "")).mkString(" "))
        println("")

      case _ => showUsage
    }
  }

  def generate(name: String, sql: String): Unit = {
    val version = DateTime.now.toString("yyyyMMddHHmmss")
    val file = new File(s"src/main/resources/db/migration/V${version}__${name}.sql")
    writeIfAbsent(file, sql)
  }

}
