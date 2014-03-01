package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldSspGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldSspGenerator

  describe("/_form.html.ssp") {
    it("should be created as expected") {
      val code = generator.formHtmlCode("members", "member", Seq(
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
          |<div class="form-group">
          |  <label class="control-label" for="name">
          |    ${s.i18n.get("member.name")}
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
          |    ${s.i18n.get("member.favoriteNumber")}
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
          |    ${s.i18n.get("member.magicNumber")}
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
          |    ${s.i18n.get("member.isActivated")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="checkbox" name="is_activated" value="true" #if(s.params.is_activated==Some(true)) checked #end />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label">
          |    ${s.i18n.get("member.birthday")}
          |  </label>
          |  <div class="controls row">
          |    <div class="${if(keyAndErrorMessages.hasErrors("birthday")) "has-error" else ""}">
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_year"  class="form-control" value="${s.params.birthday_year}"  placeholder="${s.i18n.get("year")}"  maxlength=4 />
          |      </div>
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_month" class="form-control" value="${s.params.birthday_month}" placeholder="${s.i18n.get("month")}" maxlength=2 />
          |      </div>
          |      <div class="col-xs-2">
          |        <input type="text" name="birthday_day"   class="form-control" value="${s.params.birthday_day}"   placeholder="${s.i18n.get("day")}"   maxlength=2 />
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
          |  <input type="submit" class="btn btn-primary" value="${s.i18n.get("submit")}">
          |  <a class="btn btn-default" href="${url(MembersController.indexUrl)}">${s.i18n.get("cancel")}</a>
          |</div>
          |</form>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/new.html.ssp") {
    it("should be created as expected") {
      val code = generator.newHtmlCode("members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |
          |<h3>${s.i18n.get("member.new")}</h3>
          |<hr/>
          |
          |<%--
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |--%>
          |
          |<form method="post" action="${url(MembersController.createUrl)}" class="form">
          | ${include("_form.html.ssp")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/edit.html.ssp") {
    it("should be created as expected") {
      val code = generator.editHtmlCode("members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |
          |<h3>${s.i18n.get("member.edit")}</h3>
          |<hr/>
          |
          |<%--
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |--%>
          |
          |<form method="post" action="${url(MembersController.updateUrl, "id" -> s.params.id.get.toString)}" class="form">
          | ${include("_form.html.ssp")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/index.html.ssp") {
    it("should be created as expected") {
      val code = generator.indexHtmlCode("members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val s: skinny.Skinny %>
          |<%@val members: Seq[model.Member] %>
          |<%@val totalPages: Int %>
          |
          |<h3>${s.i18n.get("member.list")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
          |
          |#if (totalPages > 1)
          |  <ul class="pagination">
          |    <li>
          |      <a href="${url(MembersController.indexUrl, "page" -> 1.toString)}">&laquo;</a>
          |    </li>
          |    #for (i <- (1 to totalPages))
          |      <li>
          |        <a href="${url(MembersController.indexUrl, "page" -> i.toString)}">${i}</a>
          |      </li>
          |    #end
          |    <li>
          |      <a href="${url(MembersController.indexUrl, "page" -> totalPages.toString)}">&raquo;</a>
          |    </li>
          |  </ul>
          |#end
          |
          |<table class="table table-bordered">
          |<thead>
          |  <tr>
          |    <th>${s.i18n.get("member.id")}</th>
          |    <th>${s.i18n.get("member.name")}</th>
          |    <th>${s.i18n.get("member.favoriteNumber")}</th>
          |    <th>${s.i18n.get("member.magicNumber")}</th>
          |    <th>${s.i18n.get("member.isActivated")}</th>
          |    <th>${s.i18n.get("member.birthday")}</th>
          |    <th></th>
          |  </tr>
          |</thead>
          |<tbody>
          |  #for (member <- members)
          |  <tr>
          |    <td>${member.id}</td>
          |    <td>${member.name}</td>
          |    <td>${member.favoriteNumber}</td>
          |    <td>${member.magicNumber}</td>
          |    <td>${member.isActivated}</td>
          |    <td>${member.birthday}</td>
          |    <td>
          |      <a href="${url(MembersController.showUrl, "id" -> member.id.toString)}" class="btn btn-default">${s.i18n.get("detail")}</a>
          |      <a href="${url(MembersController.editUrl, "id" -> member.id.toString)}" class="btn btn-info">${s.i18n.get("edit")}</a>
          |      <a data-method="delete" data-confirm="${s.i18n.get("member.delete.confirm")}"
          |        href="${url(MembersController.deleteUrl, "id" -> member.id.toString)}" rel="nofollow" class="btn btn-danger">${s.i18n.get("delete")}</a>
          |    </td>
          |  </tr>
          |  #end
          |</tbody>
          |</table>
          |
          |<a href="${url(MembersController.newUrl)}" class="btn btn-primary">${s.i18n.get("new")}</a>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/show.html.ssp") {
    it("should be created as expected") {
      val code = generator.showHtmlCode("members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))
      val expected =
        """<%@val member: model.Member %>
          |<%@val s: skinny.Skinny %>
          |
          |<h3>${s.i18n.get("member.detail")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
          |<table class="table table-bordered">
          |<thead>
          |  <tr>
          |    <th>${s.i18n.get("member.id")}</th>
          |    <td>${member.id}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.get("member.name")}</th>
          |    <td>${member.name}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.get("member.favoriteNumber")}</th>
          |    <td>${member.favoriteNumber}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.get("member.magicNumber")}</th>
          |    <td>${member.magicNumber}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.get("member.isActivated")}</th>
          |    <td>${member.isActivated}</td>
          |  </tr>
          |  <tr>
          |    <th>${s.i18n.get("member.birthday")}</th>
          |    <td>${member.birthday}</td>
          |  </tr>
          |
          |</tbody>
          |</table>
          |
          |<hr/>
          |<div class="form-actions">
          |  <a class="btn btn-default" href="${url(MembersController.indexUrl)}">${s.i18n.get("backToList")}</a>
          |  <a href="${url(MembersController.editUrl, "id" -> member.id.toString)}" class="btn btn-info">${s.i18n.get("edit")}</a>
          |  <a data-method="delete" data-confirm="${s.i18n.get("member.delete.confirm")}"
          |    href="${url(MembersController.deleteUrl, "id" -> member.id.toString)}" rel="nofollow" class="btn btn-danger">${s.i18n.get("delete")}</a>
          |</div>
          |""".stripMargin
      code should equal(expected)
    }
  }
}
