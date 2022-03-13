package skinny.task.generator

import skinny.ParamType
import java.io.File

import org.apache.commons.io.FileUtils
import scalikejdbc.ConnectionPool
import skinny.nlp.Inflector

/**
  * Model generator.
  */
object ModelGenerator extends ModelGenerator {
  override def withTimestamps: Boolean = true
}

trait ModelGenerator extends CodeGenerator {

  def withId: Boolean = true

  def withTimestamps: Boolean = true

  def useAutoConstruct: Boolean = false

  def primaryKeyName: String = "id"

  def primaryKeyType: ParamType = ParamType.Long

  def tableName: Option[String] = None

  private[this] def showUsage = {
    showSkinnyGenerator()
    println("""  Usage: sbt "task/run generate:model member name:String birthday:Option[LocalDate]""")
    println("""         sbt "task/run generate:model admin.legacy member name:String birthday:Option[LocalDate]""")
    println("")
  }

  def run(args: Seq[String]): Unit = {
    val completedArgs: List[String] = {
      if (args.size >= 2 && args(1).contains(":")) Seq("") ++ args
      else args
    }.toList
    completedArgs match {
      case namespace :: name :: attributes =>
        showSkinnyGenerator()
        val nameAndTypeNamePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(name, typeName, columnDef) => Some(name -> typeName)
            case Array(name, typeName)            => Some(name -> typeName)
            case _                                => None
          }
        }
        generate(namespace.split('.'), name, tableName, nameAndTypeNamePairs)
        generateSpec(namespace.split('.'), name, nameAndTypeNamePairs)
        println("")

      case _ => showUsage
    }
  }

  def code(namespaces: Seq[String],
           name: String,
           tableName: Option[String],
           nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val namespace      = toNamespace(modelPackage, namespaces)
    val modelClassName = toClassName(name)
    val alias = {
      val a = modelClassName.filter(_.isUpper).map(_.toLower).mkString
      if (CodeGenerator.SQLReservedWords.contains(a)) a + "_" else a
    }
    val timestampPrefix              = if (withId) ",\n" else { if (nameAndTypeNamePairs.isEmpty) "" else ",\n" }
    val caseClassFieldsPrimaryKeyRow = if (withId) s"""  ${primaryKeyName}: ${primaryKeyType}""" else ""
    val extractorsPrimaryKeyRow      = if (withId) s"""    ${primaryKeyName} = rs.get(rn.${primaryKeyName})""" else ""
    val attributePrefix              = if (withId) ",\n" else ""
    val mapperClassName              = if (withId) "SkinnyCRUDMapper" else "SkinnyNoIdCRUDMapper"
    val timestampsTraitIfExists      = if (withTimestamps) s"with TimestampsFeature[${modelClassName}] " else ""

    def isHasManyThrough(entityName: String, modelClassName: String): Boolean = {
      entityName.startsWith(Inflector.pluralize(modelClassName)) ||
      entityName.endsWith(Inflector.pluralize(modelClassName)) ||
      entityName.startsWith(Inflector.singularize(modelClassName)) ||
      entityName.endsWith(Inflector.singularize(modelClassName))
    }
    def singularizeHasManyThroughTypeName(entityName: String, modelClassName: String): String = {
      if (entityName.startsWith(Inflector.pluralize(modelClassName))
          || entityName.startsWith(Inflector.singularize(modelClassName))) {
        val second = Inflector.singularize(
          entityName
            .replaceFirst(Inflector.pluralize(modelClassName), "")
            .replaceFirst(Inflector.singularize(modelClassName), "")
        )
        modelClassName + second
      } else if (entityName.endsWith(Inflector.pluralize(modelClassName))
                 || entityName.endsWith(Inflector.singularize(modelClassName))) {
        val first = Inflector.singularize(
          entityName
            .replaceFirst(Inflector.pluralize(modelClassName), "")
            .replaceFirst(Inflector.singularize(modelClassName), "")
        )
        first + modelClassName
      } else {
        entityName
      }
    }
    def toManyThroughNameAndTypeName(entityName: String): (String, String) = {
      val _entityName = entityName.replaceFirst(modelClassName, "")
      val _name       = toFirstCharLower(Inflector.pluralize(_entityName))
      (_name, _entityName)
    }
    def filterHasManyThrough(nameAntTypeName: (String, String), modelClassName: String): (String, String) = {
      val entityName = extractTypeIfOptionOrSeq(nameAntTypeName._2)
      if (isHasManyThrough(entityName, modelClassName)) {
        val (name, typeName) = toManyThroughNameAndTypeName(
          singularizeHasManyThroughTypeName(entityName, modelClassName)
        )
        (name, s"Seq[${typeName}]")
      } else {
        nameAntTypeName
      }
    }

    val timestamps = if (withTimestamps) {
      s"""${timestampPrefix}  createdAt: DateTime,
         |  updatedAt: DateTime""".stripMargin
    } else ""

    val timestampsExtraction = if (withTimestamps) {
      s"""${timestampPrefix}    createdAt = rs.get(rn.createdAt),
         |    updatedAt = rs.get(rn.updatedAt)""".stripMargin
    } else ""

    val customConnectionPoolName = {
      if (connectionPoolName != null && connectionPoolName != ConnectionPool.DEFAULT_NAME) {
        if (connectionPoolName.isInstanceOf[Symbol]) {
          "\n  override lazy val connectionPoolName = " + connectionPoolName
        } else {
          "\n  override lazy val connectionPoolName = \"" + connectionPoolName + "\""
        }
      } else ""
    }

    val customPkName = {
      if (primaryKeyName != "id") "\n  override lazy val primaryKeyFieldName = \"" + primaryKeyName + "\""
      else ""
    }

    val caseClassFields = s"""${caseClassFieldsPrimaryKeyRow}${if (nameAndTypeNamePairs.isEmpty) ""
                             else {
                               nameAndTypeNamePairs
                                 .map((v) => filterHasManyThrough(v, modelClassName))
                                 .map { case (name, typeName) => CodeGenerator.convertReservedWord(name) -> typeName }
                                 .map {
                                   case (name, typeName) =>
                                     s"  ${name}: ${toScalaTypeNameWithDefaultValueIfOptionOrSeq(typeName)}"
                                 }
                                 .mkString(attributePrefix, ",\n", "")
                             }}${timestamps}
        |""".stripMargin

    val nameConverters = {
      val parts = nameAndTypeNamePairs
        .map(_._1)
        .flatMap { name =>
          CodeGenerator.reservedWordConversionRules.find { case (k, _) => k == name }
        }
        .map {
          case (original, converted) =>
            s""""^${converted}$$" -> "${original}""""
        }
      if (parts.isEmpty) {
        ""
      } else {
        s"""
           |  override lazy val nameConverters = Map(${parts.mkString(", ")})""".stripMargin
      }
    }

    val extractors =
      s"""${extractorsPrimaryKeyRow}${if (nameAndTypeNamePairs.isEmpty) ""
         else {
           nameAndTypeNamePairs
             .filterNot { case (_, typeName) => isAssociationTypeName(typeName) }
             .map {
               case (name, typeName) => "    " + name + " = rs.get(rn." + name + ")"
             }
             .mkString(attributePrefix, ",\n", "")
         }}${timestampsExtraction}
        |""".stripMargin

    val primaryKeyTypeIfNotLong =
      if (primaryKeyType == ParamType.Long) ""
      else
        s"""
         |  override def idToRawValue(id: String): Any = id
         |  override def rawValueToId(value: Any): String = value.toString
         |  override def useExternalIdGenerator = true
         |  override def generateId = java.util.UUID.randomUUID.toString""".stripMargin

    val associationNameAndTypeNamePairs = nameAndTypeNamePairs
      .filter { case (_, typeName) => isAssociationTypeName(typeName) }

    val associationCaseClassFields = associationNameAndTypeNamePairs
      .map {
        case (name, typeName) =>
          val entityName = extractTypeIfOptionOrSeq(typeName)
          if (isHasManyThrough(entityName, modelClassName)) {
            toManyThroughNameAndTypeName(singularizeHasManyThroughTypeName(entityName, modelClassName))
          } else {
            (name, typeName)
          }
      }

    val associations = {
      if (associationNameAndTypeNamePairs.isEmpty) {
        ""
      } else {
        associationNameAndTypeNamePairs
          .map {
            case (name, typeName) if typeName.startsWith("Option[") =>
              val entityName  = extractTypeIfOptionOrSeq(typeName)
              val entityAlias = toFirstCharUpper(name).filter(_.isUpper).map(_.toLower).mkString
              s"  lazy val ${name}Ref = belongsTo[${entityName}](${entityName}, (${alias}, ${entityAlias}) => ${alias}.copy(${name} = ${entityAlias}))"
            case (name, typeName) if typeName.startsWith("Seq[") =>
              val entityName = extractTypeIfOptionOrSeq(typeName)
              if (isHasManyThrough(entityName, modelClassName)) {
                val throughEntity        = singularizeHasManyThroughTypeName(entityName, modelClassName)
                val (_name, _entityName) = toManyThroughNameAndTypeName(throughEntity)
                val entityAlias          = toFirstCharUpper(_entityName).filter(_.isUpper).map(_.toLower).mkString
                s"""  lazy val ${_name}Ref = hasManyThrough[${_entityName}](
               |    through = ${throughEntity},
               |    many = ${_entityName},
               |    merge = (${alias}, ${entityAlias}s) => ${alias}.copy(${_name} = ${entityAlias}s)
               |  )""".stripMargin
              } else {
                val entityAlias  = toFirstCharUpper(name).filter(_.isUpper).map(_.toLower).mkString
                val entityFkName = toFirstCharLower(modelClassName) + toFirstCharUpper(primaryKeyName)
                s"""  lazy val ${name}Ref = hasMany[${entityName}](
               |    many = ${entityName} -> ${entityName}.defaultAlias,
               |    on = (${alias}, ${entityAlias}) => sqls.eq(${alias}.${primaryKeyName}, ${entityAlias}.${entityFkName}),
               |    merge = (${alias}, ${entityAlias}s) => ${alias}.copy(${name} = ${entityAlias}s)
               |  )""".stripMargin
              }
          }
          .mkString("\n", "\n\n", "\n")
      }
    }

    val extractMethod = {
      if (useAutoConstruct) {
        val associationFields = {
          val result = associationCaseClassFields
            .map { case (name, _) => "\"" + name + "\"" }
            .mkString(", ")
          if (result.isEmpty) "" else ", " + result
        }
        s"""  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = {
        |    autoConstruct(rs, rn${associationFields})
        |  }""".stripMargin

      } else {
        s"""  /*
        |   * If you're familiar with ScalikeJDBC/Skinny ORM, using #autoConstruct makes your mapper simpler.
        |   * (e.g.)
        |   * override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]) = autoConstruct(rs, rn)
        |   *
        |   * Be aware of excluding associations like this:
        |   * (e.g.)
        |   * case class Member(id: Long, companyId: Long, company: Option[Company] = None)
        |   * object Member extends SkinnyCRUDMapper[Member] {
        |   *   override def extract(rs: WrappedResultSet, rn: ResultName[Member]) =
        |   *     autoConstruct(rs, rn, "company") // "company" will be skipped
        |   * }
        |   */
        |  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = new ${modelClassName}(
        |${extractors}  )""".stripMargin
      }
    }

    s"""package ${namespace}
        |
        |import skinny.orm._, feature._
        |import scalikejdbc._
        |import org.joda.time._
        |
        |case class ${modelClassName}(
        |${caseClassFields})
        |
        |object ${modelClassName} extends ${mapperClassName}${if (primaryKeyType == ParamType.Long)
         s"[${modelClassName}]"
       else s"WithId[${primaryKeyType}, ${modelClassName}]"} ${timestampsTraitIfExists}{
        |${tableName.map(t => "  override lazy val tableName = \"" + t + "\"").getOrElse("")}
        |  override lazy val defaultAlias = createAlias("${alias}")${customConnectionPoolName}${customPkName}${primaryKeyTypeIfNotLong}${nameConverters}
        |${associations}
        |${extractMethod}
        |}
        |""".stripMargin
  }

  def generate(namespaces: Seq[String],
               name: String,
               tableName: Option[String],
               nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val productionFile = new File(
      s"${sourceDir}/${toDirectoryPath(modelPackageDir, namespaces)}/${toClassName(name)}.scala"
    )
    writeIfAbsent(productionFile, code(namespaces, name, tableName, nameAndTypeNamePairs))
  }

  def spec(namespaces: Seq[String], name: String): String = {
    s"""package ${toNamespace(modelPackage, namespaces)}
        |
        |import skinny.DBSettings
        |import skinny.test._
        |import org.scalatest.flatspec.FixtureAnyFlatSpec
        |import org.scalatest.matchers.should.Matchers
        |import scalikejdbc._
        |import scalikejdbc.scalatest._
        |import org.joda.time._
        |
        |class ${toClassName(name)}Spec extends FixtureAnyFlatSpec with Matchers with DBSettings with AutoRollback {
        |}
        |""".stripMargin
  }

  def generateSpec(namespaces: Seq[String], name: String, nameAndTypeNamePairs: Seq[(String, String)]): Unit = {
    val specFile = new File(
      s"${testSourceDir}/${toDirectoryPath(modelPackageDir, namespaces)}/${toClassName(name)}Spec.scala"
    )
    FileUtils.forceMkdir(specFile.getParentFile)
    writeIfAbsent(specFile, spec(namespaces, name))
  }

}
