package skinny.task

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Scaffold generator.
 */
trait ScaffoldGenerator {

  def run(args: List[String]) {
    if (args.size < 3) {
      showUsage
      return
    } else if (args.head.contains(":") || args.tail.head.contains(":")) {
      showUsage
      return
    }

    args match {
      case resources :: resource :: attributes =>
        val attributePairs: Seq[(String, String)] = attributes.flatMap { attribute =>
          attribute.toString.split(":") match {
            case Array(k, v) => Some(k -> v)
            case _ => None
          }
        }
        // Controller
        generateController(resources, resource, attributePairs)
        generateControllerSpec(resources, resource, attributePairs)

        // Model
        generateModel(resource, attributePairs)
        generateModelSpec(resource, attributePairs)

        // Views
        generateNewView(resources, resource, attributePairs)
        generateEditView(resources, resource, attributePairs)
        generateIndexView(resources, resource, attributePairs)
        generateShowView(resources, resource, attributePairs)

        // messages.conf
        generateMessages(resources, resource, attributePairs)

        // SQL
        generateSQLs(resources, resource, attributePairs)

      case _ =>
        println("Usage: ./skinny scaffold members member firstName:String lastName:String")
    }
  }

  def generateController(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val file = new File(s"src/main/scala/controller/${controllerClassName}.scala")
    FileUtils.forceMkdir(file.getParentFile)
    val code =
      s"""package controller
            |
            |import skinny._
            |import skinny.validator._
            |import model.${modelClassName}
            |
            |object ${controllerClassName} extends SkinnyResource {
            |  protectFromForgery()
            |
            |  override def model = ${modelClassName}
            |  override def resourcesName = "${resources}"
            |  override def resourceName = "${resource}"
            |
            |  override def createForm = validation(
            |${attributePairs.filterNot { case (_, t) => isOption(t) }.map { case (k, t) => "    paramKey(\"" + k + "\") is required" }.mkString(",\n")}
            |  )
            |  override def createFormStrongParameters = Seq(
            |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
            |  )
            |
            |  override def updateForm = validation(
            |${attributePairs.filterNot { case (_, t) => isOption(t) }.map { case (k, t) => "    paramKey(\"" + k + "\") is required" }.mkString(",\n")}
            |  )
            |  override def updateFormStrongParameters = Seq(
            |${attributePairs.map { case (k, t) => "    \"" + k + "\" -> ParamType." + toParamType(t) }.mkString(",\n")}
            |  )
            |
            |}
            |""".stripMargin
    FileUtils.write(file, code)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateControllerSpec(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val controllerClassName = toClassName(resources) + "Controller"
    val modelClassName = toClassName(resource)
    val file = new File(s"src/test/scala/controller/${controllerClassName}Spec.scala")
    FileUtils.forceMkdir(file.getParentFile)
    val code =
      s"""package controller
        |
        |import org.scalatra.test.scalatest._
        |import skinny.test._
        |import model._
        |
        |class ${controllerClassName}Spec extends ScalatraFlatSpec {
        |  addFilter(${controllerClassName}, "/*")
        |
        |  it should "show ${resources}" in {
        |    get("/${resources}") {
        |      status should equal(200)
        |    }
        |    get("/${resources}/") {
        |      status should equal(200)
        |    }
        |    get("/${resources}.json") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |    get("/${resources}.xml") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show a ${resource} in detail" in {
        |    get(s"/${resources}/$${${resource}.id}") {
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.xml") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |    get(s"/${resources}/$${${resource}.id}.json") {
        |      logger.debug(body)
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "show new entry form" in {
        |    get(s"/${resources}/new") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "create a ${resource}" in {
        |    post(s"/${resources}",
        |      ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      post(s"/${resources}",
        |        ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}, "csrfToken" -> "12345") {
        |        status should equal(302)
        |        val id = header("Location").split("/").last.toLong
        |        ${modelClassName}.findById(id).isDefined should equal(true)
        |      }
        |    }
        |  }
        |
        |  it should "show the edit form" in {
        |    get(s"/${resources}/$${${resource}.id}/edit") {
        |      status should equal(200)
        |    }
        |  }
        |
        |  it should "update a ${resource}" in {
        |    put(s"/${resources}/$${${resource}.id}",
        |      ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}) {
        |      status should equal(403)
        |    }
        |
        |    withSession("csrfToken" -> "12345") {
        |      put(s"/${resources}/$${${resource}.id}", "
        |        ${attributePairs.map { case (k, _) => "\"" + k + "\" -> null" }.mkString(",")}, "csrfToken" -> "12345") {
        |        status should equal(200)
        |      }
        |    }
        |  }
        |
        |  it should "delete a ${resource}" in {
        |    val ${resource} = FactoryGirl(${modelClassName}).create()
        |    delete(s"/${resources}/$${${resource}.id}") {
        |      status should equal(403)
        |    }
        |    withSession("csrfToken" -> "aaaaaa") {
        |      delete(s"/${resources}/$${${resource}.id}?csrfToken=aaaaaa") {
        |        status should equal(200)
        |      }
        |    }
        |  }
        |
        |}
        |""".stripMargin
    FileUtils.write(file, code)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateModel(resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val file = new File(s"src/main/scala/model/${modelClassName}.scala")
    FileUtils.forceMkdir(file.getParentFile)
    val code =
      s"""package model
            |
            |import skinny.orm._, feature._
            |import scalikejdbc._, SQLInterpolation._
            |import org.joda.time._
            |
            |case class ${modelClassName}(
            |  id: Long,
            |${attributePairs.map { case (k, t) => s"  ${k}: ${withDefaultValueIfOption(t)}" }.mkString(",\n")},
            |  createdAt: DateTime,
            |  updatedAt: Option[DateTime] = None
            |)
            |
            |object ${modelClassName} extends SkinnyCRUDMapper[${modelClassName}] with TimestampsFeature[${modelClassName}] {
            |
            |  override val defaultAlias = createAlias("${modelClassName.head.toLower}")
            |
            |  override def extract(rs: WrappedResultSet, rn: ResultName[${modelClassName}]): ${modelClassName} = new ${modelClassName}(
            |    id = rs.long(rn.id),
            |${attributePairs.map { case (k, t) => "    " + k + "= rs." + toExtractor(t) + "(rn." + k + ")" }.mkString(",\n")},
            |    createdAt = rs.dateTime(rn.createdAt),
            |    updatedAt = rs.dateTimeOpt(rn.updatedAt)
            |  )
            |}
            |""".stripMargin
    FileUtils.write(file, code)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateModelSpec(resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val file = new File(s"src/test/scala/model/${modelClassName}Spec.scala")
    FileUtils.forceMkdir(file.getParentFile)
    val code =
      s"""package model
            |
            |import org.scalatra.test.scalatest._
            |import skinny.test._
            |import scalikejdbc._, SQLInterpolation._, test._
            |import org.joda.time._
            |import model._
            |
            |class ${modelClassName}Spec extends ScalatraFlatSpec with AutoRollback {
            |}
            |""".stripMargin
    FileUtils.write(file, code)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???
  def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]): Unit = ???

  def generateMessages(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val _resources = toClassName(resources)
    val _resource = toClassName(resource)
    val messages =
      s"""
        |${resource} {
        |  flash {
        |    created="The ${resource} was created."
        |    updated="The ${resource} was updated."
        |    deleted="The ${resource} was deleted."
        |  }
        |  list="${_resources}"
        |  detail="${_resource}"
        |  edit="Edit ${_resource}"
        |  new="New ${_resource}"
        |  delete.confirm="Are you sure?"
        |${attributePairs.map { case (k, _) => "  " + k + "=\"" + toClassName(k) + "\"" }.mkString("\n")}
        |}
        |""".stripMargin
    val file = new File(s"src/main/resources/messages.conf")
    FileUtils.write(file, messages, true)
    println(s"${file.getAbsolutePath} is created.")
  }

  def generateSQLs(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    // TODO
  }

  protected def showUsage = {
    println("Usage: sbt \"task/run scaffold members member name:String birthday:Option[DateTime]\"")
  }

  protected def toClassName(name: String) = name.head.toUpper + name.tail

  protected def isOption(t: String): Boolean = t.trim().startsWith("Option")

  protected def toParamType(t: String): String = t.replaceFirst("Option\\[", "").replaceFirst("\\]", "").trim()

  protected def withDefaultValueIfOption(t: String): String = {
    if (t.startsWith("Option")) s"${t.trim()} = None" else t.trim()
  }

  protected def toExtractor(t: String): String = {
    val method = toParamType(t).head.toLower + toParamType(t).tail
    if (t.startsWith("Option")) method + "Opt"
    else method
  }

}

object ScaffoldSspGenerator extends ScaffoldSspGenerator

/**
 * Scaffold with ssp template.
 */
trait ScaffoldSspGenerator extends ScaffoldGenerator {

  override def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))

    val newSsp =
      s"""<%@val params: skinny.Params %>
            |<%@val errorMessages: Seq[String] %>
            |<%@val i18n: skinny.I18n %>
            |<%@val csrfKey: String %>
            |<%@val csrfToken: String %>
            |
            |<h3>$${i18n.get("${resource}.new")}</h3>
            |<hr/>
            |
            |#for (e <- errorMessages)
            |<p class="alert alert-danger">$${e}</p>
            |#end
            |
            |<form method="post" action="$${uri("/${resources}")}" class="form">
            |<div class="form-group">
            |  <label class="control-label" for="name">
            |    $${i18n.get("${resource}.name")}
            |  </label>
            |  <div class="controls">
            |    <div class="row col-md-12">
            |    <input type="text" name="name" class="input-lg col-lg-6" value="$${params.name}" />
            |    </div>
            |  </div>
            |</div>
            |<div class="form-group">
            |  <label class="control-label" for="url">
            |    $${i18n.get("${resource}.url")}
            |  </label>
            |  <div class="controls">
            |    <div class="row col-md-12">
            |      <input type="text" name="url" class="input-lg col-lg-8" value="$${params.url}" />
            |    </div>
            |  </div>
            |</div>
            |<input type="hidden" name="$${csrfKey}" value="$${csrfToken}"/>
            |
            |<div class="form-actions">
            |  <input type="submit" class="btn btn-primary" value="$${i18n.get("submit")}" />
            |  <a class="btn btn-default" href="$${uri("/${resources}")}">$${i18n.get("cancel")}</a>
            |</div>
            |</form>
            |""".stripMargin
    val file = new File(s"${viewDir}/new.html.ssp")
    FileUtils.write(file, newSsp)
    println(s"${file.getAbsolutePath} is created.")
  }

  override def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val editSsp =
      s"""<%@val params: skinny.Params %>
            |<%@val errorMessages: Seq[String] %>
            |<%@val i18n: skinny.I18n %>
            |<%@val csrfKey: String %>
            |<%@val csrfToken: String %>
            |
            |<h3>$${i18n.get("${resource}.edit")}</h3>
            |<hr/>
            |
            |#for (e <- errorMessages)
            |<p class="alert alert-danger">$${e}</p>
            |#end
            |
            |<form method="post" action="$${uri("/${resources}/"+params.id.get)}" class="form">
            |<div class="form-group">
            |  <label class="control-label" for="name">
            |    $${i18n.get("${resource}.name")}
            |  </label>
            |  <div class="controls">
            |    <div class="row col-md-12">
            |    <input type="text" name="name" class="input-lg col-lg-6" value="$${params.name}" />
            |    </div>
            |  </div>
            |</div>
            |<div class="form-group">
            |  <label class="control-label" for="url">
            |    $${i18n.get("${resource}.url")}
            |  </label>
            |  <div class="controls">
            |    <div class="row col-md-12">
            |      <input type="text" name="url" class="input-lg col-lg-8" value="$${params.url}" />
            |    </div>
            |  </div>
            |</div>
            |<input type="hidden" name="$${csrfKey}" value="$${csrfToken}"/>
            |
            |<div class="form-actions">
            |  <input type="submit" class="btn btn-primary" value="$${i18n.get("submit")}"/>
            |  <a class="btn btn-default" href="$${uri("/${resources}")}">$${i18n.get("cancel")}</a>
            |</div>
            |</form>
            | """.stripMargin
    val file = new File(s"${viewDir}/edit.html.ssp")
    FileUtils.write(file, editSsp)
    println(s"${file.getAbsolutePath} is created.")
  }

  override def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val indexSsp =
      s"""<%@val params: skinny.Params %>
            |<%@val flash: skinny.Flash %>
            |<%@val ${resources}: Seq[model.${modelClassName}] %>
            |<%@val i18n: skinny.I18n %>
            |
            |<h3>$${i18n.get("${resource}.list")}</h3>
            |<hr/>
            |#for (notice <- flash.notice)
            |  <p class="alert alert-info">$${notice}</p>
            |#end
            |<table class="table table-bordered">
            |<thead>
            |  <tr>
            |${attributePairs.map { case (k, _) => "    <th>${i18n.get(\"" + resource + "." + k + "\")}</th>" }.mkString(",\n")}
            |    <th></th>
            |  </tr>
            |</thead>
            |<tbody>
            |  #for (${resource} <- ${resources})
            |  <tr>
            |${attributePairs.map { case (k, _) => "    <td>${" + resource + "." + k + "}</td>" }.mkString(",\n")}
            |    <td>
            |      <a href="$${uri("/${resources}/"+${resource}.id)}" class="btn btn-default">$${i18n.get("detail")}</a>
            |      <a href="$${uri("/${resources}/"+${resource}.id+"/edit")}" class="btn btn-info">$${i18n.get("edit")}</a>
            |      <a data-method="delete" data-confirm="$${i18n.get("${resource}.delete.confirm")}"
            |        href="$${uri("/${resources}/"+${resource}.id)}" rel="nofollow" class="btn btn-danger">$${i18n.get("delete")}</a>
            |    </td>
            |  </tr>
            |  #end
            |</tbody>
            |</table>
            |
            |<a href="$${uri("/${resources}/new")}" class="btn btn-primary">$${i18n.get("new")}</a>
            |""".stripMargin
    val file = new File(s"${viewDir}/index.html.ssp")
    FileUtils.write(file, indexSsp)
    println(s"${file.getAbsolutePath} is created.")
  }

  override def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val showSsp =
      s"""<%@val ${resource}: model.Company %>
              |<%@val i18n: skinny.I18n %>
              |<%@val flash: skinny.Flash %>
              |
              |<h3>$${i18n.get("${resource}.detail")}</h3>
              |<hr/>
              |#for (notice <- flash.notice)
              |  <p class="alert alert-info">$${notice}</p>
              |#end
              |<table class="table table-bordered">
              |<thead>
              |  <tr>
              |    <th>$${i18n.get("${resource}.id")}</th>
              |    <td>$${${resource}.id}</td>
              |  </tr>
              |  <tr>
              |    <th>$${i18n.get("${resource}.name")}</th>
              |    <td>$${${resource}.name}</td>
              |  </tr>
              |  <tr>
              |    <th>$${i18n.get("${resource}.url")}</th>
              |    <td>$${${resource}.url}</td>
              |  </tr>
              |</tbody>
              |</table>
              |
              |<hr/>
              |<div class="form-actions">
              |  <a class="btn btn-default" href="$${uri("/${resources}")}">$${i18n.get("backToList")}</a>
              |  <a href="$${uri("/${resources}/"+${resource}.id+"/edit")}" class="btn btn-info">$${i18n.get("edit")}</a>
              |  <a data-method="delete" data-confirm="$${i18n.get("${resource}.delete.confirm")}"
              |    href="$${uri("/${resources}/"+${resource}.id)}" rel="nofollow" class="btn btn-danger">$${i18n.get("delete")}</a>
              |</div>
              |""".stripMargin
    val file = new File(s"${viewDir}/show.html.ssp")
    FileUtils.write(file, showSsp)
    println(s"${file.getAbsolutePath} is created.")
  }

}

object ScaffoldScamlGenerator extends ScaffoldScamlGenerator

trait ScaffoldScamlGenerator extends ScaffoldGenerator {
  // TODO
}

object ScaffoldJadeGenerator extends ScaffoldJadeGenerator

trait ScaffoldJadeGenerator extends ScaffoldGenerator {
  // TODO
}
