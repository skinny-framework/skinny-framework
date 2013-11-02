package skinny.task.generator

import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Scaffold generator with scaml template.
 */
object ScaffoldScamlGenerator extends ScaffoldScamlGenerator

/**
 * Scaffold generator with scaml template.
 */
trait ScaffoldScamlGenerator extends ScaffoldGenerator {

  protected override def template: String = "scaml"

  private[this] def formInputsPart(resource: String, attributePairs: Seq[(String, String)]) = {
    attributePairs.toList.map { case (k, t) => k -> toParamType(t) }.map {
      case (name, "Boolean") =>
        s""" %div(class="form-group")
        |  %label(class="control-label" for="${name}") #{s.i18n.get("${resource}.${name}")}
        |  %div(class="controls")
        |   %div(class="row col-md-12")
        |    %input(type="checkbox" name="${name}" value="true" checked={s.params.${name}==Some(true)})
        |""".stripMargin
      case (name, _) =>
        s""" %div(class="form-group")
        |  %label(class="control-label" for="${name}") #{s.i18n.get("${resource}.${name}")}
        |  %div(class="controls")
        |   %div(class="row col-md-12")
        |    %input(type="text" name="${name}" class="input-lg col-lg-6" value={s.params.${name}})
        |""".stripMargin
    }.mkString
  }

  override def generateNewView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))

    val newScaml =
      s"""-@val s: skinny.Skinny
        |
        |%h3 #{s.i18n.get("${resource}.new")}
        |%hr
        |
        |-for (e <- s.errorMessages)
        | %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={uri("/${resources}")} class="form")
        |${formInputsPart(resource, attributePairs)}
        | != s.csrfHiddenInputTag
        | %div(class="form-actions")
        |  %input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
        |  %a(class="btn btn-default" href={uri("/${resources}")}) #{s.i18n.get("cancel")}
        |""".stripMargin
    val file = new File(s"${viewDir}/new.html.scaml")
    FileUtils.write(file, newScaml)
    println("\"" + file.getPath + "\" created.")
  }

  override def generateEditView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val editScaml =
      s"""-@val s: skinny.Skinny
        |
        |%h3 #{s.i18n.get("${resource}.edit")}
        |%hr
        |
        |-for (e <- s.errorMessages)
        | %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={uri("/${resources}/" + s.params.id.get)} class="form")
        |${formInputsPart(resource, attributePairs)}
        | != s.csrfHiddenInputTag
        | %div(class="form-actions")
        |  %input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
        |  %a(class="btn btn-default" href={uri("/${resources}")}) #{s.i18n.get("cancel")}
        | """.stripMargin
    val file = new File(s"${viewDir}/edit.html.scaml")
    FileUtils.write(file, editScaml)
    println("\"" + file.getPath + "\" created.")
  }

  override def generateIndexView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))
    val indexScaml =
      s"""-@val s: skinny.Skinny
        |-@val ${resources}: Seq[model.${modelClassName}]
        |
        |%h3 #{s.i18n.get("${resource}.list")}
        |%hr
        |-for (notice <- s.flash.notice)
        | %p(class="alert alert-info") #{notice}
        |
        |%table(class="table table-bordered")
        | %thead
        |  %tr
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "   %th #{s.i18n.get(\"" + resource + "." + k + "\")}" }.mkString("\n")}
        |   %th
        | %tbody
        | -for (${resource} <- ${resources})
        |  %tr
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "   %td #{" + resource + "." + k + "}" }.mkString("\n")}
        |   %td
        |    %a(href={uri("/${resources}/" + ${resource}.id)} class="btn btn-default") #{s.i18n.get("detail")}
        |    %a(href={uri("/${resources}/" + ${resource}.id + "/edit")} class="btn btn-info") #{s.i18n.get("edit")}
        |    %a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={uri("/${resources}/" + ${resource}.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |
        |%a(href={uri("/${resources}/new")} class="btn btn-primary") #{s.i18n.get("new")}
        |""".stripMargin
    val file = new File(s"${viewDir}/index.html.scaml")
    FileUtils.write(file, indexScaml)
    println("\"" + file.getPath + "\" created.")
  }

  override def generateShowView(resources: String, resource: String, attributePairs: Seq[(String, String)]) {
    val modelClassName = toClassName(resource)
    val viewDir = s"src/main/webapp/WEB-INF/views/${resources}"
    FileUtils.forceMkdir(new File(viewDir))

    val attributesPart = (("id" -> "Long") :: attributePairs.toList).map {
      case (name, _) =>
        s"""  %tr
        |   %th #{s.i18n.get("${resource}.${name}")}
        |   %td #{${resource}.${name}}
        |""".stripMargin
    }.mkString

    val showScaml =
      s"""-@val ${resource}: model.${modelClassName}
        |-@val s: skinny.Skinny
        |
        |%h3 #{s.i18n.get("${resource}.detail")}
        |%hr
        |-for (notice <- s.flash.notice)
        | %p(class="alert alert-info") #{notice}
        |%table(class="table table-bordered")
        | %thead
        |${attributesPart}
        |
        |%hr
        |%div(class="form-actions")
        | %a(class="btn btn-default" href={uri("/${resources}")}) #{s.i18n.get("backToList")}
        | %a(href={uri("/${resources}/" + ${resource}.id + "/edit")} class="btn btn-info") #{s.i18n.get("edit")}
        | %a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={uri("/${resources}/"+${resource}.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |""".stripMargin
    val file = new File(s"${viewDir}/show.html.scaml")
    FileUtils.write(file, showScaml)
    println("\"" + file.getPath + "\" created.")
  }

}
