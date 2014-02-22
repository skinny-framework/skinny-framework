package skinny.task.generator

/**
 * Scaffold generator with jade template.
 */
object ScaffoldJadeGenerator extends ScaffoldJadeGenerator

/**
 * Scaffold generator with jade template.
 */
trait ScaffoldJadeGenerator extends ScaffoldGenerator {

  protected override def template: String = "jade"

  override def formHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    "-@val s: skinny.Skinny\n" +
      "-@val keyAndErrorMessages: skinny.KeyAndErrorMessages\n\n" +
      attributePairs.toList.map { case (k, t) => (k, toParamType(t)) }.map {
        case (name, "Boolean") =>
          s"""div(class="form-group")
          |  label(class="control-label" for="${name}") #{s.i18n.get("${resource}.${name}")}
          |  div(class="controls row")
          |    div(class="col-xs-12")
          |      input(type="checkbox" name="${name}" class="form-control" value="true" checked={s.params.${name}==Some(true)})
          |""".stripMargin
        case (name, "DateTime") =>
          s"""div(class="form-group")
          |  label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("${name}")) "has-error" else ""})
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Year"   class="form-control" value={s.params.${name}Year}   placeholder={s.i18n.get("year")}   maxlength=4)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Month"  class="form-control" value={s.params.${name}Month}  placeholder={s.i18n.get("month")}  maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Day"    class="form-control" value={s.params.${name}Day}    placeholder={s.i18n.get("day")}    maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Hour"   class="form-control" value={s.params.${name}Hour}   placeholder={s.i18n.get("hour")}   maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Minute" class="form-control" value={s.params.${name}Minute} placeholder={s.i18n.get("minute")} maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Second" class="form-control" value={s.params.${name}Second} placeholder={s.i18n.get("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${name}").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalDate") =>
          s"""div(class="form-group")
          |  label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("${name}")) "has-error" else ""})
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Year"  class="form-control" value={s.params.${name}Year}  placeholder={s.i18n.get("year")}  maxlength=4)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Month" class="form-control" value={s.params.${name}Month} placeholder={s.i18n.get("month")} maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Day"   class="form-control" value={s.params.${name}Day}   placeholder={s.i18n.get("day")}   maxlength=2)
          |    - keyAndErrorMessages.get("${name}").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, "LocalTime") =>
          s"""div(class="form-group")
          |  label(class="control-label") #{s.i18n.get("${resource}.${name}")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("${name}")) "has-error" else ""})
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Hour"   class="form-control" value={s.params.${name}Hour}   placeholder={s.i18n.get("hour")}   maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Minute" class="form-control" value={s.params.${name}Minute} placeholder={s.i18n.get("minute")} maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="${name}Second" class="form-control" value={s.params.${name}Second} placeholder={s.i18n.get("second")} maxlength=2)
          |    - keyAndErrorMessages.get("${name}").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |""".stripMargin
        case (name, _) =>
          s"""div(class="form-group")
          |  label(class="control-label" for="${name}") #{s.i18n.get("${resource}.${name}")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("${name}")) "has-error" else ""})
          |      div(class="col-xs-12")
          |        input(type="text" name="${name}" class="form-control" value={s.params.${name}})
          |    - keyAndErrorMessages.get("${name}").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |""".stripMargin
      }.mkString +
      s"""div(class="form-actions")
        |  =unescape(s.csrfHiddenInputTag)
        |  input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
        |    a(class="btn btn-default" href={url(${controllerClassName}.indexUrl)}) #{s.i18n.get("cancel")}
        |""".stripMargin
  }

  override def newHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""-@val s: skinny.Skinny
        |
        |h3 #{s.i18n.get("${resource}.new")}
        |hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  p(class="alert alert-danger") #{e}
        |
        |form(method="post" action={url(${controllerClassName}.createUrl)} class="form")
        |  =include("_form.html.jade")
        |""".stripMargin
  }

  override def editHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""-@val s: skinny.Skinny
        |
        |h3 #{s.i18n.get("${resource}.edit")}
        |hr
        |
        |-#-for (e <- s.errorMessages)
        |-#  p(class="alert alert-danger") #{e}
        |
        |form(method="post" action={url(${controllerClassName}.updateUrl, "id" -> s.params.id.get.toString)} class="form")
        |  =include("_form.html.jade")
        |""".stripMargin
  }

  override def indexHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)
    s"""-@val s: skinny.Skinny
        |-@val ${resources}: Seq[model.${modelClassName}]
        |
        |h3 #{s.i18n.get("${resource}.list")}
        |hr
        |-for (notice <- s.flash.notice)
        |  p(class="alert alert-info") #{notice}
        |
        |table(class="table table-bordered")
        |  thead
        |    tr
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "      th #{s.i18n.get(\"" + resource + "." + k + "\")}" }.mkString("\n")}
        |      th
        |  tbody
        |  -for (${resource} <- ${resources})
        |    tr
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "      td #{" + resource + "." + k + "}" }.mkString("\n")}
        |      td
        |        a(href={url(${controllerClassName}.showUrl, "id" -> ${resource}.id.toString)} class="btn btn-default") #{s.i18n.get("detail")}
        |        a(href={url(${controllerClassName}.editUrl, "id" -> ${resource}.id.toString)} class="btn btn-info") #{s.i18n.get("edit")}
        |        a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={url(${controllerClassName}.deleteUrl, "id" -> ${resource}.id.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |
        |a(href={url(${controllerClassName}.newUrl)} class="btn btn-primary") #{s.i18n.get("new")}
        |""".stripMargin
  }

  override def showHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)
    val attributesPart = (("id" -> "Long") :: attributePairs.toList).map {
      case (name, _) =>
        s"""    tr
        |      th #{s.i18n.get("${resource}.${name}")}
        |      td #{${resource}.${name}}
        |""".stripMargin
    }.mkString

    s"""-@val ${resource}: model.${modelClassName}
        |-@val s: skinny.Skinny
        |
        |h3 #{s.i18n.get("${resource}.detail")}
        |hr
        |-for (notice <- s.flash.notice)
        |  p(class="alert alert-info") #{notice}
        |table(class="table table-bordered")
        |  thead
        |${attributesPart}
        |hr
        |div(class="form-actions")
        |  a(class="btn btn-default" href={url(${controllerClassName}.indexUrl)}) #{s.i18n.get("backToList")}
        |  a(href={url(${controllerClassName}.editUrl, "id" -> ${resource}.id.toString)} class="btn btn-info") #{s.i18n.get("edit")}
        |  a(data-method="delete" data-confirm={s.i18n.get("${resource}.delete.confirm")} href={url(${controllerClassName}.deleteUrl, "id" -> ${resource}.id.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
        |""".stripMargin
  }

}
