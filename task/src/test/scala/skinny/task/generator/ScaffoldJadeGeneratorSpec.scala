package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldJadeGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldJadeGenerator

  describe("/_form.html.jade") {
    it("should be created as expected") {
      val code = generator.formHtmlCode("members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-@val s: skinny.Skinny
          |-@val keyAndErrorMessages: skinny.KeyAndErrorMessages
          |
          |div(class="form-group")
          |  label(class="control-label" for="name") #{s.i18n.get("member.name")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("name")) "has-error" else ""})
          |      div(class="col-xs-12")
          |        input(type="text" name="name" class="form-control" value={s.params.name})
          |    - keyAndErrorMessages.get("name").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |div(class="form-group")
          |  label(class="control-label" for="favorite_number") #{s.i18n.get("member.favoriteNumber")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("favorite_number")) "has-error" else ""})
          |      div(class="col-xs-12")
          |        input(type="text" name="favorite_number" class="form-control" value={s.params.favorite_number})
          |    - keyAndErrorMessages.get("favorite_number").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |div(class="form-group")
          |  label(class="control-label" for="magic_number") #{s.i18n.get("member.magicNumber")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("magic_number")) "has-error" else ""})
          |      div(class="col-xs-12")
          |        input(type="text" name="magic_number" class="form-control" value={s.params.magic_number})
          |    - keyAndErrorMessages.get("magic_number").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |div(class="form-group")
          |  label(class="control-label" for="is_activated") #{s.i18n.get("member.isActivated")}
          |  div(class="controls row")
          |    div(class="col-xs-12")
          |      input(type="checkbox" name="is_activated" class="form-control" value="true" checked={s.params.is_activated==Some(true)})
          |div(class="form-group")
          |  label(class="control-label") #{s.i18n.get("member.birthday")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("birthday")) "has-error" else ""})
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_year"   class="form-control" value={s.params.birthday_year}   placeholder={s.i18n.get("year")}   maxlength=4)
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_month"  class="form-control" value={s.params.birthday_month}  placeholder={s.i18n.get("month")}  maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_day"    class="form-control" value={s.params.birthday_day}    placeholder={s.i18n.get("day")}    maxlength=2)
          |    - keyAndErrorMessages.get("birthday").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |div(class="form-actions")
          |  =unescape(s.csrfHiddenInputTag)
          |  input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
          |    a(class="btn btn-default" href={url(MembersController.indexUrl)}) #{s.i18n.get("cancel")}
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
        """-@val s: skinny.Skinny
          |
          |h3 #{s.i18n.get("member.new")}
          |hr
          |
          |-#-for (e <- s.errorMessages)
          |-#  p(class="alert alert-danger") #{e}
          |
          |form(method="post" action={url(MembersController.createUrl)} class="form")
          |  =include("_form.html.jade")
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
        """-@val s: skinny.Skinny
          |
          |h3 #{s.i18n.get("member.edit")}
          |hr
          |
          |-#-for (e <- s.errorMessages)
          |-#  p(class="alert alert-danger") #{e}
          |
          |form(method="post" action={url(MembersController.updateUrl, "id" -> s.params.id.get.toString)} class="form")
          |  =include("_form.html.jade")
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
        """-@val s: skinny.Skinny
          |-@val members: Seq[model.Member]
          |-@val totalPages: Int
          |
          |h3 #{s.i18n.get("member.list")}
          |hr
          |-for (notice <- s.flash.notice)
          |  p(class="alert alert-info") #{notice}
          |
          |- if (totalPages > 1)
          |  ul.pagination
          |    li
          |      a(href={url(MembersController.indexUrl, "page" -> 1.toString)}) &laquo;
          |    - for (i <- (1 to totalPages))
          |      li
          |        a(href={url(MembersController.indexUrl, "page" -> i.toString)}) #{i}
          |    li
          |      a(href={url(MembersController.indexUrl, "page" -> totalPages.toString)}) &raquo;
          |
          |table(class="table table-bordered")
          |  thead
          |    tr
          |      th #{s.i18n.get("member.id")}
          |      th #{s.i18n.get("member.name")}
          |      th #{s.i18n.get("member.favoriteNumber")}
          |      th #{s.i18n.get("member.magicNumber")}
          |      th #{s.i18n.get("member.isActivated")}
          |      th #{s.i18n.get("member.birthday")}
          |      th
          |  tbody
          |  -for (member <- members)
          |    tr
          |      td #{member.id}
          |      td #{member.name}
          |      td #{member.favoriteNumber}
          |      td #{member.magicNumber}
          |      td #{member.isActivated}
          |      td #{member.birthday}
          |      td
          |        a(href={url(MembersController.showUrl, "id" -> member.id.toString)} class="btn btn-default") #{s.i18n.get("detail")}
          |        a(href={url(MembersController.editUrl, "id" -> member.id.toString)} class="btn btn-info") #{s.i18n.get("edit")}
          |        a(data-method="delete" data-confirm={s.i18n.get("member.delete.confirm")} href={url(MembersController.deleteUrl, "id" -> member.id.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
          |
          |a(href={url(MembersController.newUrl)} class="btn btn-primary") #{s.i18n.get("new")}
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
        """-@val member: model.Member
          |-@val s: skinny.Skinny
          |
          |h3 #{s.i18n.get("member.detail")}
          |hr
          |-for (notice <- s.flash.notice)
          |  p(class="alert alert-info") #{notice}
          |table(class="table table-bordered")
          |  thead
          |    tr
          |      th #{s.i18n.get("member.id")}
          |      td #{member.id}
          |    tr
          |      th #{s.i18n.get("member.name")}
          |      td #{member.name}
          |    tr
          |      th #{s.i18n.get("member.favoriteNumber")}
          |      td #{member.favoriteNumber}
          |    tr
          |      th #{s.i18n.get("member.magicNumber")}
          |      td #{member.magicNumber}
          |    tr
          |      th #{s.i18n.get("member.isActivated")}
          |      td #{member.isActivated}
          |    tr
          |      th #{s.i18n.get("member.birthday")}
          |      td #{member.birthday}
          |
          |hr
          |div(class="form-actions")
          |  a(class="btn btn-default" href={url(MembersController.indexUrl)}) #{s.i18n.get("backToList")}
          |  a(href={url(MembersController.editUrl, "id" -> member.id.toString)} class="btn btn-info") #{s.i18n.get("edit")}
          |  a(data-method="delete" data-confirm={s.i18n.get("member.delete.confirm")} href={url(MembersController.deleteUrl, "id" -> member.id.toString)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
          |""".stripMargin
      code should equal(expected)
    }
  }
}
