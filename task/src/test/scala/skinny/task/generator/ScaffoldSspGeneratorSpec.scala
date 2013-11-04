package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldSspGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldSspGenerator

  describe("/_form.html.scaml") {
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
          |
          |<div class="form-group">
          |  <label class="control-label" for="name">
          |    ${s.i18n.get("member.name")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="text" name="name" class="form-control" value="${s.params.name}" />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="favoriteNumber">
          |    ${s.i18n.get("member.favoriteNumber")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="text" name="favoriteNumber" class="form-control" value="${s.params.favoriteNumber}" />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="magicNumber">
          |    ${s.i18n.get("member.magicNumber")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="text" name="magicNumber" class="form-control" value="${s.params.magicNumber}" />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label" for="isActivated">
          |    ${s.i18n.get("member.isActivated")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-12">
          |      <input type="checkbox" name="isActivated" value="true" #if(s.params.isActivated == Some(true)) checked #end />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-group">
          |  <label class="control-label">
          |    ${s.i18n.get("member.birthday")}
          |  </label>
          |  <div class="controls row">
          |    <div class="col-xs-2">
          |      <input type="text" name="birthdayYear"  class="form-control" value="${s.params.birthdayYear}"  placeholder="${s.i18n.get("year")}"  maxlength=4 />
          |    </div>
          |    <div class="col-xs-2">
          |      <input type="text" name="birthdayMonth" class="form-control" value="${s.params.birthdayMonth}" placeholder="${s.i18n.get("month")}" maxlength=2 />
          |    </div>
          |    <div class="col-xs-2">
          |      <input type="text" name="birthdayDay"   class="form-control" value="${s.params.birthdayDay}"   placeholder="${s.i18n.get("day")}"   maxlength=2 />
          |    </div>
          |  </div>
          |</div>
          |<div class="form-actions">
          |  ${unescape(s.csrfHiddenInputTag)}
          |  <input type="submit" class="btn btn-primary" value="${s.i18n.get("submit")}">
          |  <a class="btn btn-default" href="${uri("/members")}">${s.i18n.get("cancel")}</a>
          |</div>
          |</form>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/new.html.jade") {
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
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |
          |<form method="post" action="${uri("/members")}" class="form">
          | ${include("_form.html.ssp")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/edit.html.jade") {
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
          |#for (e <- s.errorMessages)
          |<p class="alert alert-danger">${e}</p>
          |#end
          |
          |<form method="post" action="${uri("/members/" + s.params.id.get)}" class="form">
          | ${include("_form.html.ssp")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/index.html.jade") {
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
          |
          |<h3>${s.i18n.get("member.list")}</h3>
          |<hr/>
          |#for (notice <- s.flash.notice)
          |  <p class="alert alert-info">${notice}</p>
          |#end
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
          |      <a href="${uri("/members/" + member.id)}" class="btn btn-default">${s.i18n.get("detail")}</a>
          |      <a href="${uri("/members/" + member.id + "/edit")}" class="btn btn-info">${s.i18n.get("edit")}</a>
          |      <a data-method="delete" data-confirm="${s.i18n.get("member.delete.confirm")}"
          |        href="${uri("/members/" + member.id)}" rel="nofollow" class="btn btn-danger">${s.i18n.get("delete")}</a>
          |    </td>
          |  </tr>
          |  #end
          |</tbody>
          |</table>
          |
          |<a href="${uri("/members/new")}" class="btn btn-primary">${s.i18n.get("new")}</a>
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/show.html.jade") {
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
          |  <a class="btn btn-default" href="${uri("/members")}">${s.i18n.get("backToList")}</a>
          |  <a href="${uri("/members/" + member.id + "/edit")}" class="btn btn-info">${s.i18n.get("edit")}</a>
          |  <a data-method="delete" data-confirm="${s.i18n.get("member.delete.confirm")}"
          |    href="${uri("/members/" + member.id)}" rel="nofollow" class="btn btn-danger">${s.i18n.get("delete")}</a>
          |</div>
          |""".stripMargin
      code should equal(expected)
    }
  }
}
