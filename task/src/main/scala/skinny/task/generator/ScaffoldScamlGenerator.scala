package skinny.task.generator

import skinny.controller.Params

/**
 * Scaffold generator with scaml template.
 */
object ScaffoldScamlGenerator extends ScaffoldScamlGenerator

/**
 * Scaffold generator with scaml template.
 */
trait ScaffoldScamlGenerator extends ScaffoldGenerator {

  protected override def template: String = "scaml"

  override def formHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    "-@val s: skinny.Skinny\n" +
      "-@val keyAndErrorMessages: skinny.KeyAndErrorMessages\n\n" +
      s"- import ${toNamespace("controller", namespaces)}.${controllerClassName}\n\n" +
      attributePairs.toList.map { case (k, t) => (k, toParamType(t)) }.map {
        case (name, "Boolean") =>
          s"""%div(class="form-group")
           |  %label(class="control-label" for="${toSnakeCase(name)}") #{s.i18n.get("${resource}.${name}")}
           |  %div(class="controls row")
           |    %div(class="col-xs-12")
           |      %input(type="checkbox" name="${toSnakeCase(name)}" class="form-control" value="true" checked={s.params.${toSnakeCase(name)}==Some(true)})
           |""".stripMargin
        case (name, "DateTime") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Year)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Year)}}   placeholder={s.i18n.get("year")}   maxlength=4)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Month)}"  class="form-control" value={s.params.${toSnakeCase(name + Params.Month)}}  placeholder={s.i18n.get("month")}  maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Day)}"    class="form-control" value={s.params.${toSnakeCase(name + Params.Day)}}    placeholder={s.i18n.get("day")}    maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Hour)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Hour)}}   placeholder={s.i18n.get("hour")}   maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Minute)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Minute)}} placeholder={s.i18n.get("minute")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Second)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Second)}} placeholder={s.i18n.get("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalDate") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Year)}"  class="form-control" value={s.params.${toSnakeCase(name + Params.Year)}}  placeholder={s.i18n.get("year")}  maxlength=4)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Month)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Month)}} placeholder={s.i18n.get("month")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Day)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Day)}}   placeholder={s.i18n.get("day")}   maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalTime") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Hour)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Hour)}}   placeholder={s.i18n.get("hour")}   maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Minute)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Minute)}} placeholder={s.i18n.get("minute")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Second)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Second)}} placeholder={s.i18n.get("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, _) =>
          s"""%div(class="form-group")
          |  %label(class="control-label" for="${toSnakeCase(name)}") #{s.i18n.get("${resource}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-12")
          |        %input(type="text" name="${toSnakeCase(name)}" class="form-control" value={s.params.${toSnakeCase(name)}})
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
      }.mkString +
      s"""%div(class="form-actions")
        |  =unescape(s.csrfHiddenInputTag)
        |  %input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
        |    %a(class="btn btn-default" href={url(${controllerClassName}.indexUrl)}) #{s.i18n.get("cancel")}
        |""".stripMargin
  }

  override def newHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""-@val s: skinny.Skinny
        |
        |- import ${toNamespace("controller", namespaces)}.${controllerClassName}
        |
        |%h3 #{s.i18n.get("${resource}.new")}
        |%hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={url(${controllerClassName}.createUrl)} class="form")
        |  =include("_form.html.scaml")
        |""".stripMargin
  }

  override def editHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""-@val s: skinny.Skinny
        |
        |- import ${toNamespace("controller", namespaces)}.${controllerClassName}
        |
        |%h3 #{s.i18n.get("${resource}.edit")}
        |%hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={url(${controllerClassName}.updateUrl, "${snakeCasedPrimaryKeyName}" -> s.params.${snakeCasedPrimaryKeyName}.get.toString)} class="form")
        |  =include("_form.html.scaml")
        |""".stripMargin
  }

  override def indexHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)
    s"""-@val s: skinny.Skinny
        |-@val items: Seq[${toNamespace("model", namespaces)}.${modelClassName}]
        |-@val totalPages: Int
        |
        |- import ${toNamespace("controller", namespaces)}.${controllerClassName}
        |
        |%h3 #{s.i18n.get("${resource}.list")}
        |%hr
        |-for (notice <- s.flash.notice)
        |  %p(class="alert alert-info") #{notice}
        |
        |- if (totalPages > 1)
        |  %ul.pagination
        |    %li
        |      %a(href={url(${controllerClassName}.indexUrl, "page" -> 1.toString)}) &laquo;
        |    - for (i <- (1 to totalPages))
        |      %li
        |        %a(href={url(${controllerClassName}.indexUrl, "page" -> i.toString)}) #{i}
        |    %li
        |      %a(href={url(${controllerClassName}.indexUrl, "page" -> totalPages.toString)}) &raquo;
        |
        |%table(class="table table-bordered")
        |  %thead
        |    %tr
        |${((primaryKeyName -> "Long") :: attributePairs.toList).map { case (k, _) => "      %th #{s.i18n.get(\"" + resource + "." + k + "\")}" }.mkString("\n")}
        |      %th
        |  %tbody
        |  -for (item <- items)
        |    %tr
        |${((primaryKeyName -> "Long") :: attributePairs.toList).map { case (k, _) => "      %td #{item." + k + "}" }.mkString("\n")}
        |      %td
        |        %a(href={url(${controllerClassName}.showUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName}.toString)} class="btn btn-default") #{s.i18n.get("detail")}
        |        %a(href={url(${controllerClassName}.editUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName}.toString)} class="btn btn-info") #{s.i18n.get("edit")}
        |        %a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={url(${controllerClassName}.deleteUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName}.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |
        |%a(href={url(${controllerClassName}.newUrl)} class="btn btn-primary") #{s.i18n.get("new")}
        |""".stripMargin
  }

  override def showHtmlCode(namespaces: Seq[String], resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)

    val attributesPart = ((primaryKeyName -> "Long") :: attributePairs.toList).map {
      case (name, _) =>
        s"""    %tr
        |      %th #{s.i18n.get("${resource}.${name}")}
        |      %td #{item.${name}}
        |""".stripMargin
    }.mkString

    s"""-@val item: ${toNamespace("model", namespaces)}.${modelClassName}
        |-@val s: skinny.Skinny
        |
        |- import ${toNamespace("controller", namespaces)}.${controllerClassName}
        |
        |%h3 #{s.i18n.get("${resource}.detail")}
        |%hr
        |-for (notice <- s.flash.notice)
        |  %p(class="alert alert-info") #{notice}
        |%table(class="table table-bordered")
        |  %thead
        |${attributesPart}
        |%hr
        |%div(class="form-actions")
        |  %a(class="btn btn-default" href={url(${controllerClassName}.indexUrl)}) #{s.i18n.get("backToList")}
        |  %a(href={url(${controllerClassName}.editUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName}.toString)} class="btn btn-info") #{s.i18n.get("edit")}
        |  %a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={url(${controllerClassName}.deleteUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName}.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |""".stripMargin
  }

}
