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

  val packageImportsWarning =
    s"""-# Be aware of package imports.
       |-# 1. ${sourceDir}/templates/ScalatePackage.scala
       |-# 2. scalateTemplateConfig in project/Build.scala""".stripMargin

  override def formHtmlCode(namespaces: Seq[String], resources: String, resource: String, nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val controllerName = "Controllers." + toControllerName(namespaces, resources)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)
    "-@val s: skinny.Skinny\n" +
      "-@val keyAndErrorMessages: skinny.KeyAndErrorMessages\n\n" +
      packageImportsWarning + "\n\n" +
      nameAndTypeNamePairs.toList.map { case (k, t) => (k, extractTypeIfOptionOrSeq(t)) }.map {
        case (name, "Boolean") =>
          s"""%div(class="form-group")
           |  %label(class="control-label" for="${toSnakeCase(name)}") #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
           |  %div(class="controls row")
           |    %div(class="col-xs-12")
           |      %input(type="checkbox" name="${toSnakeCase(name)}" class="form-control" value="true" checked={s.params.${toSnakeCase(name)}==Some(true)})
           |""".stripMargin
        case (name, "DateTime") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Year)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Year)}}   placeholder={s.i18n.getOrKey("year")}   maxlength=4)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Month)}"  class="form-control" value={s.params.${toSnakeCase(name + Params.Month)}}  placeholder={s.i18n.getOrKey("month")}  maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Day)}"    class="form-control" value={s.params.${toSnakeCase(name + Params.Day)}}    placeholder={s.i18n.getOrKey("day")}    maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Hour)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Hour)}}   placeholder={s.i18n.getOrKey("hour")}   maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Minute)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Minute)}} placeholder={s.i18n.getOrKey("minute")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Second)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Second)}} placeholder={s.i18n.getOrKey("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalDate") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Year)}"  class="form-control" value={s.params.${toSnakeCase(name + Params.Year)}}  placeholder={s.i18n.getOrKey("year")}  maxlength=4)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Month)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Month)}} placeholder={s.i18n.getOrKey("month")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Day)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Day)}}   placeholder={s.i18n.getOrKey("day")}   maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalTime") =>
          s"""%div(class="form-group")
          |  %label(class="control-label") #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
          |  %div(class="controls row")
          |    %div(class={if(keyAndErrorMessages.hasErrors("${toSnakeCase(name)}")) "has-error" else ""})
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Hour)}"   class="form-control" value={s.params.${toSnakeCase(name + Params.Hour)}}   placeholder={s.i18n.getOrKey("hour")}   maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Minute)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Minute)}} placeholder={s.i18n.getOrKey("minute")} maxlength=2)
          |      %div(class="col-xs-2")
          |        %input(type="text" name="${toSnakeCase(name + Params.Second)}" class="form-control" value={s.params.${toSnakeCase(name + Params.Second)}} placeholder={s.i18n.getOrKey("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${toSnakeCase(name)}").map { errors =>
          |      %div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          %label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, _) =>
          s"""%div(class="form-group")
          |  %label(class="control-label" for="${toSnakeCase(name)}") #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
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
        |  %input(type="submit" class="btn btn-primary" value={s.i18n.getOrKey("submit")})
        |    %a(class="btn btn-default" href={s.url(${controllerName}.indexUrl)}) #{s.i18n.getOrKey("cancel")}
        |""".stripMargin
  }

  override def newHtmlCode(namespaces: Seq[String], resources: String, resource: String, nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val controllerName = "Controllers." + toControllerName(namespaces, resources)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)
    s"""-@val s: skinny.Skinny
        |
        |${packageImportsWarning}
        |
        |%h3 #{s.i18n.getOrKey("${resourceWithNamespace}.new")}
        |%hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={s.url(${controllerName}.createUrl)} class="form")
        |  =include("_form.html.scaml")
        |""".stripMargin
  }

  override def editHtmlCode(namespaces: Seq[String], resources: String, resource: String, nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val controllerName = "Controllers." + toControllerName(namespaces, resources)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)
    s"""-@val s: skinny.Skinny
        |
        |${packageImportsWarning}
        |
        |%h3 #{s.i18n.getOrKey("${resourceWithNamespace}.edit")} : ##{s.params.id}
        |%hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  %p(class="alert alert-danger") #{e}
        |
        |%form(method="post" action={s.url(${controllerName}.updateUrl, "${snakeCasedPrimaryKeyName}" -> s.params.${snakeCasedPrimaryKeyName})} class="form")
        |  =include("_form.html.scaml")
        |""".stripMargin
  }

  override def indexHtmlCode(namespaces: Seq[String], resources: String, resource: String, nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val controllerName = "Controllers." + toControllerName(namespaces, resources)
    val modelClassName = toClassName(resource)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)
    val operations = {
      if (operationLinksInIndexPageRequired) {
        s"""|        %a(href={s.url(${controllerName}.showUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} class="btn btn-default") #{s.i18n.getOrKey("detail")}
            |        %a(href={s.url(${controllerName}.editUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} class="btn btn-info") #{s.i18n.getOrKey("edit")}
            |        %a(data-method="delete" data-confirm={s.i18n.getOrKey("${resourceWithNamespace}.delete.confirm")} href={s.url(${controllerName}.destroyUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} rel="nofollow" class="btn btn-danger") #{s.i18n.getOrKey("delete")}""".stripMargin
      } else {
        s"""|        %a(href={s.url(${controllerName}.showUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} class="btn btn-default") #{s.i18n.getOrKey("detail")}""".stripMargin
      }
    }
    s"""-@val s: skinny.Skinny
        |-@val items: Seq[${toNamespace(modelPackage, namespaces)}.${modelClassName}]
        |-@val totalPages: Int
        |-@val page: Int = s.params.page.map(_.toString.toInt).getOrElse(1)
        |
        |${packageImportsWarning}
        |
        |%h3 #{s.i18n.getOrKey("${resourceWithNamespace}.list")}
        |%hr
        |-for (notice <- s.flash.notice)
        |  %p(class="alert alert-info") #{notice}
        |
        |- if (totalPages > 1)
        |  %ul.pagination
        |    %li
        |      %a(href={s.url(${controllerName}.indexUrl, "page" -> 1)}) &laquo;
        |    -val maxPage = Math.min(totalPages, if (page <= 5) 11 else page + 5)
        |    -for (i <- Math.max(1, maxPage - 10) to maxPage)
        |      %li(class={if (i == page) "active" else ""})
        |        %a(href={s.url(${controllerName}.indexUrl, "page" -> i)}) #{i}
        |    %li
        |      %a(href={s.url(${controllerName}.indexUrl, "page" -> totalPages)}) &raquo;
        |    %li
        |      %span #{Math.min(page, totalPages)} / #{totalPages}
        |
        |%p(class="pull-right")
        |  %a(href={s.url(${controllerName}.newUrl)} class="btn btn-primary") #{s.i18n.getOrKey("new")}
        |
        |%table(class="table table-bordered")
        |  %thead
        |    %tr
        |${((primaryKeyName -> "Long") :: nameAndTypeNamePairs.toList).map { case (k, _) => "      %th #{s.i18n.getOrKey(\"" + resourceWithNamespace + "." + k + "\")}" }.mkString("\n")}
        |      %th
        |  %tbody
        |  -for (item <- items)
        |    %tr
        |${((primaryKeyName -> "Long") :: nameAndTypeNamePairs.toList).map { case (k, _) => "      %td #{item." + k + "}" }.mkString("\n")}
        |      %td
        |${operations}
        |  -if (items.isEmpty)
        |    %tr
        |      %td(colspan="${2 + nameAndTypeNamePairs.size}") #{s.i18n.getOrKey("empty")}
        |
        |%a(href={s.url(${controllerName}.newUrl)} class="btn btn-primary") #{s.i18n.getOrKey("new")}
        |""".stripMargin
  }

  override def showHtmlCode(namespaces: Seq[String], resources: String, resource: String, nameAndTypeNamePairs: Seq[(String, String)]): String = {
    val controllerName = "Controllers." + toControllerName(namespaces, resources)
    val modelClassName = toClassName(resource)
    val resourceWithNamespace = toResourceNameWithNamespace(namespaces, resource)

    val attributesPart = ((primaryKeyName -> "Long") :: nameAndTypeNamePairs.toList).map {
      case (name, _) =>
        s"""    %tr
        |      %th #{s.i18n.getOrKey("${resourceWithNamespace}.${name}")}
        |      %td #{item.${name}}
        |""".stripMargin
    }.mkString

    s"""-@val item: ${toNamespace(modelPackage, namespaces)}.${modelClassName}
        |-@val s: skinny.Skinny
        |
        |${packageImportsWarning}
        |
        |%h3 #{s.i18n.getOrKey("${resourceWithNamespace}.detail")}
        |%hr
        |-for (notice <- s.flash.notice)
        |  %p(class="alert alert-info") #{notice}
        |%table(class="table table-bordered")
        |  %thead
        |${attributesPart}
        |%hr
        |%div(class="form-actions")
        |  %a(class="btn btn-default" href={s.url(${controllerName}.indexUrl)}) #{s.i18n.getOrKey("backToList")}
        |  %a(href={s.url(${controllerName}.editUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} class="btn btn-info") #{s.i18n.getOrKey("edit")}
        |  %a(data-method="delete" data-confirm={s.i18n.getOrKey("${resourceWithNamespace}.delete.confirm")} href={s.url(${controllerName}.destroyUrl, "${snakeCasedPrimaryKeyName}" -> item.${primaryKeyName})} rel="nofollow" class="btn btn-danger") #{s.i18n.getOrKey("delete")}
        |""".stripMargin
  }

}
