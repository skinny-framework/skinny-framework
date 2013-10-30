package skinny.task.generator

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Scaffold generator with ssp template.
 */
object ScaffoldSspGenerator extends ScaffoldSspGenerator

/**
 * Scaffold generator with ssp template.
 */
trait ScaffoldSspGenerator extends ScaffoldGenerator {

  private def formInputsPart(resource: String, attributePairs: Seq[(String, String)]) = {
    // TODO timestamp
    attributePairs.toList.map { case (k, t) => k -> toParamType(t) }.map {
      case (name, "Boolean") =>
        s"""<div class="form-group">
        |  <label class="control-label" for="name">
        |    $${i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls">
        |    <div class="row col-md-12">
        |    <input type="checkbox" name="${name}" value="true" #if(params.${name} == Some(true)) checked #end />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
      case (name, _) =>
        s"""<div class="form-group">
        |  <label class="control-label" for="name">
        |    $${i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls">
        |    <div class="row col-md-12">
        |    <input type="text" name="${name}" class="input-lg col-lg-6" value="$${params.${name}}" />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
    }.mkString
  }

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
        |${formInputsPart(resource, attributePairs)}
        |<input type="hidden" name="$${csrfKey}" value="$${csrfToken}"/>
        |<div class="form-actions">
        |  <input type="submit" class="btn btn-primary" value="$${i18n.get("submit")}" />
        |  <a class="btn btn-default" href="$${uri("/${resources}")}">$${i18n.get("cancel")}</a>
        |</div>
        |</form>
        |""".stripMargin
    val file = new File(s"${viewDir}/new.html.ssp")
    FileUtils.write(file, newSsp)
    println("\"" + file.getPath + "\" created.")
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
        |${formInputsPart(resource, attributePairs)}
        |<input type="hidden" name="$${csrfKey}" value="$${csrfToken}"/>
        |<div class="form-actions">
        |  <input type="submit" class="btn btn-primary" value="$${i18n.get("submit")}"/>
        |  <a class="btn btn-default" href="$${uri("/${resources}")}">$${i18n.get("cancel")}</a>
        |</div>
        |</form>
        | """.stripMargin
    val file = new File(s"${viewDir}/edit.html.ssp")
    FileUtils.write(file, editSsp)
    println("\"" + file.getPath + "\" created.")
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
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "    <th>${i18n.get(\"" + resource + "." + k + "\")}</th>" }.mkString("\n")}
        |    <th></th>
        |  </tr>
        |</thead>
        |<tbody>
        |  #for (${resource} <- ${resources})
        |  <tr>
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "    <td>${" + resource + "." + k + "}</td>" }.mkString("\n")}
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
    println("\"" + file.getPath + "\" created.")
  }

  override def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))

    val attributesPart = (("id" -> "Long") :: attributePairs.toList).map {
      case (name, _) =>
        s"""  <tr>
        |    <th>$${i18n.get("${resource}.${name}")}</th>
        |    <td>$${${resource}.${name}}</td>
        |  </tr>
        |""".stripMargin
    }.mkString

    val showSsp =
      s"""<%@val ${resource}: model.${modelClassName} %>
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
        |${attributesPart}
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
    println("\"" + file.getPath + "\" created.")
  }

}
