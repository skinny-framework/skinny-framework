package skinny.task.generator

/**
 * Scaffold generator with ssp template.
 */
object ScaffoldSspGenerator extends ScaffoldSspGenerator

/**
 * Scaffold generator with ssp template.
 */
trait ScaffoldSspGenerator extends ScaffoldGenerator {

  override def formHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    "<%@val s: skinny.Skinny %>\n\n" +
      attributePairs.toList.map { case (k, t) => (k, toParamType(t)) }.map {
        case (name, "Boolean") =>
          s"""<div class="form-group">
        |  <label class="control-label" for="${name}">
        |    $${s.i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls row">
        |    <div class="col-xs-12">
        |      <input type="checkbox" name="${name}" value="true" #if(s.params.${name}==Some(true)) checked #end />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
        case (name, "DateTime") =>
          s"""<div class="form-group">
        |  <label class="control-label">
        |    $${s.i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls row">
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Year"   class="form-control" value="$${s.params.${name}Year}"   placeholder="$${s.i18n.get("year")}"  maxlength=4 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Month"  class="form-control" value="$${s.params.${name}Month}"  placeholder="$${s.i18n.get("month")}" maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Day"    class="form-control" value="$${s.params.${name}Day}"    placeholder="$${s.i18n.get("day")}"   maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Hour"   class="form-control" value="$${s.params.${name}Hour}"   placeholder="$${s.i18n.get("hour")}"  maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Minute" class="form-control" value="$${s.params.${name}Minute}" placeholder="$${s.i18n.get("minute")}" maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Second" class="form-control" value="$${s.params.${name}Second}" placeholder="$${s.i18n.get("second")}" maxlength=2 />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
        case (name, "LocalDate") =>
          s"""<div class="form-group">
        |  <label class="control-label">
        |    $${s.i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls row">
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Year"  class="form-control" value="$${s.params.${name}Year}"  placeholder="$${s.i18n.get("year")}"  maxlength=4 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Month" class="form-control" value="$${s.params.${name}Month}" placeholder="$${s.i18n.get("month")}" maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Day"   class="form-control" value="$${s.params.${name}Day}"   placeholder="$${s.i18n.get("day")}"   maxlength=2 />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
        case (name, "LocalTime") =>
          s"""<div class="form-group">
        |  <label class="control-label">
        |    $${s.i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls row">
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Hour"   class="form-control" value="$${s.params.${name}Hour}"   placeholder="$${s.i18n.get("hour")}"   maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Minute" class="form-control" value="$${s.params.${name}Minute}" placeholder="$${s.i18n.get("minute")}" maxlength=2 />
        |    </div>
        |    <div class="col-xs-2">
        |      <input type="text" name="${name}Second" class="form-control" value="$${s.params.${name}Second}" placeholder="$${s.i18n.get("second")}" maxlength=2 />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
        case (name, _) =>
          s"""<div class="form-group">
        |  <label class="control-label" for="${name}">
        |    $${s.i18n.get("${resource}.${name}")}
        |  </label>
        |  <div class="controls row">
        |    <div class="col-xs-12">
        |      <input type="text" name="${name}" class="form-control" value="$${s.params.${name}}" />
        |    </div>
        |  </div>
        |</div>
        |""".stripMargin
      }.mkString +
      s"""<div class="form-actions">
        |  $${unescape(s.csrfHiddenInputTag)}
        |  <input type="submit" class="btn btn-primary" value="$${s.i18n.get("submit")}">
        |  <a class="btn btn-default" href="$${url(${controllerClassName}.indexUrl)}">$${s.i18n.get("cancel")}</a>
        |</div>
        |</form>
        |""".stripMargin
  }

  override def newHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""<%@val s: skinny.Skinny %>
        |
        |<h3>$${s.i18n.get("${resource}.new")}</h3>
        |<hr/>
        |
        |#for (e <- s.errorMessages)
        |<p class="alert alert-danger">$${e}</p>
        |#end
        |
        |<form method="post" action="$${url(${controllerClassName}.createUrl)}" class="form">
        | $${include("_form.html.ssp")}
        |""".stripMargin
  }

  override def editHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    s"""<%@val s: skinny.Skinny %>
        |
        |<h3>$${s.i18n.get("${resource}.edit")}</h3>
        |<hr/>
        |
        |#for (e <- s.errorMessages)
        |<p class="alert alert-danger">$${e}</p>
        |#end
        |
        |<form method="post" action="$${url(${controllerClassName}.updateUrl, "id" -> s.params.id.get.toString)}" class="form">
        | $${include("_form.html.ssp")}
        |""".stripMargin
  }

  override def indexHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)
    s"""<%@val s: skinny.Skinny %>
        |<%@val ${resources}: Seq[model.${modelClassName}] %>
        |
        |<h3>$${s.i18n.get("${resource}.list")}</h3>
        |<hr/>
        |#for (notice <- s.flash.notice)
        |  <p class="alert alert-info">$${notice}</p>
        |#end
        |<table class="table table-bordered">
        |<thead>
        |  <tr>
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "    <th>${s.i18n.get(\"" + resource + "." + k + "\")}</th>" }.mkString("\n")}
        |    <th></th>
        |  </tr>
        |</thead>
        |<tbody>
        |  #for (${resource} <- ${resources})
        |  <tr>
        |${(("id" -> "Long") :: attributePairs.toList).map { case (k, _) => "    <td>${" + resource + "." + k + "}</td>" }.mkString("\n")}
        |    <td>
        |      <a href="$${url(${controllerClassName}.showUrl, "id" -> ${resource}.id.toString)}" class="btn btn-default">$${s.i18n.get("detail")}</a>
        |      <a href="$${url(${controllerClassName}.editUrl, "id" -> ${resource}.id.toString)}" class="btn btn-info">$${s.i18n.get("edit")}</a>
        |      <a data-method="delete" data-confirm="$${s.i18n.get("${resource}.delete.confirm")}"
        |        href="$${url(${controllerClassName}.deleteUrl, "id" -> ${resource}.id.toString)}" rel="nofollow" class="btn btn-danger">$${s.i18n.get("delete")}</a>
        |    </td>
        |  </tr>
        |  #end
        |</tbody>
        |</table>
        |
        |<a href="$${url(${controllerClassName}.newUrl)}" class="btn btn-primary">$${s.i18n.get("new")}</a>
        |""".stripMargin
  }

  override def showHtmlCode(resources: String, resource: String, attributePairs: Seq[(String, String)]): String = {
    val controllerClassName = toControllerClassName(resources)
    val modelClassName = toClassName(resource)
    val attributesPart = (("id" -> "Long") :: attributePairs.toList).map {
      case (name, _) =>
        s"""  <tr>
        |    <th>$${s.i18n.get("${resource}.${name}")}</th>
        |    <td>$${${resource}.${name}}</td>
        |  </tr>
        |""".stripMargin
    }.mkString

    s"""<%@val ${resource}: model.${modelClassName} %>
        |<%@val s: skinny.Skinny %>
        |
        |<h3>$${s.i18n.get("${resource}.detail")}</h3>
        |<hr/>
        |#for (notice <- s.flash.notice)
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
        |  <a class="btn btn-default" href="$${url(${controllerClassName}.indexUrl)}">$${s.i18n.get("backToList")}</a>
        |  <a href="$${url(${controllerClassName}.editUrl, "id" -> ${resource}.id.toString)}" class="btn btn-info">$${s.i18n.get("edit")}</a>
        |  <a data-method="delete" data-confirm="$${s.i18n.get("${resource}.delete.confirm")}"
        |    href="$${url(${controllerClassName}.deleteUrl, "id" -> ${resource}.id.toString)}" rel="nofollow" class="btn btn-danger">$${s.i18n.get("delete")}</a>
        |</div>
        |""".stripMargin
  }

}
