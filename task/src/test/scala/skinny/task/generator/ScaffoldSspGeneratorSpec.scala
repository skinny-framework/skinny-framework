package skinny.task.generator

import org.scalatest._

class ScaffoldSspGeneratorSpec extends FunSpec with Matchers {

  val generator = ScaffoldSspGenerator

  describe("/_form.html.ssp") {
    it("should be created as expected") {
      val code = generator.formHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """<%@val s: skinny.Skinny %>
          |<%@val keyAndErrorMessages: skinny.KeyAndErrorMessages %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<div class="form-group">
          |  <label class="control-label" for="name">
          |    ${s.i18n.getOrKey("adminMember.name")}
          |  </label>
          |  <div class="controls row">
          |    <div class="${if(keyAndErrorMessages.hasErrors("name")) "has-error" else ""}">
          |      <div class="col-xs-12">
          |        <input type="text" name="name" class="form-control" value="${s.params.name}" />
          |      </div>
          |    </div>
          |    #if (keyAndErrorMessages.hasErrors("name"))
          |      <div class="col-xs-12 has-error">
          |        #for (error <- keyAndErrorMessages.getErrors("name"))
          |          <label class="control-label">${error}</label>
          |        #end
          |      </div>
          |    #end
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="favorite_number">
          |    ${s.i18n.getOrKey("adminMember.favoriteNumber")}
          |  </label>
          |  <div class="controls row">
          |    <div class="${if(keyAndErrorMessages.hasErrors("favorite_number")) "has-error" else ""}">
          |      <div class="col-xs-12">
          |        <input type="text" name="favorite_number" class="form-control" value="${s.params.favorite_number}" />
          |      </div>
          |    </div>
          |    #if (keyAndErrorMessages.hasErrors("favorite_number"))
          |      <div class="col-xs-12 has-error">
          |        #for (error <- keyAndErrorMessages.getErrors("favorite_number"))
          |          <label class="control-label">${error}</label>
          |        #end
          |      </div>
          |    #end
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="magic_number">
          |    ${s.i18n.getOrKey("adminMember.magicNumber")}
          |  </label>
          |  <div class="controls row">
          |    <div class="${if(keyAndErrorMessages.hasErrors("magic_number")) "has-error" else ""}">
          |      <div class="col-xs-12">
          |        <input type="text" name="magic_number" class="form-control" value="${s.params.magic_number}" />
          |      </div>
          |    </div>
          |    #if (keyAndErrorMessages.hasErrors("magic_number"))
          |      <div class="col-xs-12 has-error">
          |        #for (error <- keyAndErrorMessages.getErrors("magic_number"))
          |          <label class="control-label">${error}</label>
          |        #end
          |      </div>
          |    #end
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="is_activated">
          |    ${s.i18n.getOrKey("adminMember.isActivated")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="checkbox" name="is_activated" value="true" #if(s.params.is_activated==Some(true)) checked #end />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label">
          |    ${s.i18n.getOrKey("adminMember.birthday")}
          |  </label>
          |  <div class="controls row">
          |    <div class="${if(keyAndErrorMessages.hasErrors("birthday")) "has-error" else ""}">
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_year"  class="form-control" value="${s.params.birthday_year}"  placeholder="${s.i18n.getOrKey("year")}"  maxlength=4 />
          |      </div>
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_month" class="form-control" value="${s.params.birthday_month}" placeholder="${s.i18n.getOrKey("month")}" maxlength=2 />
          |      </div>
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_day"   class="form-control" value="${s.params.birthday_day}"   placeholder="${s.i18n.getOrKey("day")}"   maxlength=2 />
          |      </div>
          |    </div>
          |    #if (keyAndErrorMessages.hasErrors("birthday"))
          |      <div class="col-xs-12 has-error">
          |        #for (error <- keyAndErrorMessages.getErrors("birthday"))
          |          <label class="control-label">${error}</label>
          |        #end
          |      </div>
          |    #end
          |  </div>
          |</div>
          |<div class="form-actions">
          |  ${unescape(s.csrfHiddenInputTag)}
          |  <input type="submit" class="btn btn-primary" value="${s.i18n.getOrKey("submit")}">
          |  <a class="btn btn-default" href="${s.url(Controllers.adminMembers.indexUrl)}">${s.i18n.getOrKey("cancel")}</a>
          |</div>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/new.html.ssp") {
    it("should be created as expected") {
      val code = generator.newHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<h3>${s.i18n.getOrKey("adminMember.new")}</h3>
          |<hr/>
          |
          |<%--
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |--%>
          |
          |<form method="post" action="${s.url(Controllers.adminMembers.createUrl)}" class="form">
          | ${include("_form.html.ssp")}
          |</form>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/edit.html.ssp") {
    it("should be created as expected") {
      val code = generator.editHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<h3>${s.i18n.getOrKey("adminMember.edit")} : #${s.params.id}</h3>
          |<hr/>
          |
          |<%--
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |--%>
          |
          |<form method="post" action="${s.url(Controllers.adminMembers.updateUrl, "id" -> s.params.id)}" class="form">
          | ${include("_form.html.ssp")}
          |</form>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/index.html.ssp") {
    it("should be created as expected") {
      val code = generator.indexHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |<%@val items: Seq[model.admin.Member] %>
          |<%@val totalPages: Int %>
          |<%@val page: Int = s.params.page.map(_.toString.toInt).getOrElse(1) %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<h3>${s.i18n.getOrKey("adminMember.list")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
          |
          |#if (totalPages > 1)
          |  <ul class="pagination">
          |    <li>
          |      <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> 1)}">&laquo;</a>
          |    </li>
          |    <% val maxPage = Math.min(totalPages, if (page <= 5) 11 else page + 5) %>
          |    #for (i <- Math.max(1, maxPage - 10) to maxPage)
          |      <li class="${if (i == page) "active" else ""}">
          |        <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> i)}">${i}</a>
          |      </li>
          |    #end
          |    <li>
          |      <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> totalPages)}">&raquo;</a>
          |    </li>
          |    <li>
          |      <span>${Math.min(page, totalPages)} / ${totalPages}</span>
          |    </li>
          |  </ul>
          |#end
          |
          |<p class="pull-right">
          |  <a href="${s.url(Controllers.adminMembers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
          |</p>
          |
          |<table class="table table-bordered">
          |<thead>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.id")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.name")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.favoriteNumber")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.magicNumber")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.isActivated")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.birthday")}</th>
          |    <th></th>
          |  </tr>
          |</thead>
          |<tbody>
          |  #for (item <- items)
          |  <tr>
          |    <td>${item.id}</td>
          |    <td>${item.name}</td>
          |    <td>${item.favoriteNumber}</td>
          |    <td>${item.magicNumber}</td>
          |    <td>${item.isActivated}</td>
          |    <td>${item.birthday}</td>
          |    <td>
          |      <a href="${s.url(Controllers.adminMembers.showUrl, "id" -> item.id)}" class="btn btn-default">${s.i18n.getOrKey("detail")}</a>
          |      <a href="${s.url(Controllers.adminMembers.editUrl, "id" -> item.id)}" class="btn btn-info">${s.i18n.getOrKey("edit")}</a>
          |      <a data-method="delete" data-confirm="${s.i18n.getOrKey("adminMember.delete.confirm")}"
          |        href="${s.url(Controllers.adminMembers.destroyUrl, "id" -> item.id)}" rel="nofollow" class="btn btn-danger">${s.i18n.getOrKey("delete")}</a>
          |    </td>
          |  </tr>
          |  #end
          |  #if (items.isEmpty)
          |  <tr>
          |    <td colspan="7">${s.i18n.getOrKey("empty")}</td>
          |  </tr>
          |  #end
          |</tbody>
          |</table>
          |
          |<a href="${s.url(Controllers.adminMembers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
          |""".stripMargin
      code should equal(expected)
    }

    it("should be created as expected when disabling edit/delete links") {
      val generator = new ScaffoldSspGenerator {
        override lazy val operationLinksInIndexPageRequired = false
      }
      val code = generator.indexHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |<%@val items: Seq[model.admin.Member] %>
          |<%@val totalPages: Int %>
          |<%@val page: Int = s.params.page.map(_.toString.toInt).getOrElse(1) %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<h3>${s.i18n.getOrKey("adminMember.list")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
          |
          |#if (totalPages > 1)
          |  <ul class="pagination">
          |    <li>
          |      <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> 1)}">&laquo;</a>
          |    </li>
          |    <% val maxPage = Math.min(totalPages, if (page <= 5) 11 else page + 5) %>
          |    #for (i <- Math.max(1, maxPage - 10) to maxPage)
          |      <li class="${if (i == page) "active" else ""}">
          |        <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> i)}">${i}</a>
          |      </li>
          |    #end
          |    <li>
          |      <a href="${s.url(Controllers.adminMembers.indexUrl, "page" -> totalPages)}">&raquo;</a>
          |    </li>
          |    <li>
          |      <span>${Math.min(page, totalPages)} / ${totalPages}</span>
          |    </li>
          |  </ul>
          |#end
          |
          |<p class="pull-right">
          |  <a href="${s.url(Controllers.adminMembers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
          |</p>
          |
          |<table class="table table-bordered">
          |<thead>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.id")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.name")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.favoriteNumber")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.magicNumber")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.isActivated")}</th>
          |    <th>${s.i18n.getOrKey("adminMember.birthday")}</th>
          |    <th></th>
          |  </tr>
          |</thead>
          |<tbody>
          |  #for (item <- items)
          |  <tr>
          |    <td>${item.id}</td>
          |    <td>${item.name}</td>
          |    <td>${item.favoriteNumber}</td>
          |    <td>${item.magicNumber}</td>
          |    <td>${item.isActivated}</td>
          |    <td>${item.birthday}</td>
          |    <td>
          |      <a href="${s.url(Controllers.adminMembers.showUrl, "id" -> item.id)}" class="btn btn-default">${s.i18n.getOrKey("detail")}</a>
          |    </td>
          |  </tr>
          |  #end
          |  #if (items.isEmpty)
          |  <tr>
          |    <td colspan="7">${s.i18n.getOrKey("empty")}</td>
          |  </tr>
          |  #end
          |</tbody>
          |</table>
          |
          |<a href="${s.url(Controllers.adminMembers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/show.html.ssp") {
    it("should be created as expected") {
      val code = generator.showHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val item: model.admin.Member %>
          |<%@val s: skinny.Skinny %>
          |
          |<%-- Be aware of package imports.
          | 1. src/main/scala/templates/ScalatePackage.scala
          | 2. scalateTemplateConfig in project/Build.scala
          |--%>
          |
          |<h3>${s.i18n.getOrKey("adminMember.detail")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
          |<table class="table table-bordered">
          |<tbody>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.id")}</th>
          |    <td>${item.id}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.name")}</th>
          |    <td>${item.name}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.favoriteNumber")}</th>
          |    <td>${item.favoriteNumber}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.magicNumber")}</th>
          |    <td>${item.magicNumber}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.isActivated")}</th>
          |    <td>${item.isActivated}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.getOrKey("adminMember.birthday")}</th>
          |    <td>${item.birthday}</td>
          |  </tr>
          |
          |</tbody>
          |</table>
          |
          |<hr/>
          |<div class="form-actions">
          |  <a class="btn btn-default" href="${s.url(Controllers.adminMembers.indexUrl)}">${s.i18n.getOrKey("backToList")}</a>
          |  <a href="${s.url(Controllers.adminMembers.editUrl, "id" -> item.id)}" class="btn btn-info">${s.i18n.getOrKey("edit")}</a>
          |  <a data-method="delete" data-confirm="${s.i18n.getOrKey("adminMember.delete.confirm")}"
          |    href="${s.url(Controllers.adminMembers.destroyUrl, "id" -> item.id)}" rel="nofollow" class="btn btn-danger">${s.i18n.getOrKey("delete")}</a>
          |</div>
          |""".stripMargin
      code should equal(expected)
    }
  }
}
