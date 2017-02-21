package skinny.task.generator

import org.scalatest._

class ScaffoldJadeGeneratorSpec extends FunSpec with Matchers {

  val generator = ScaffoldJadeGenerator

  describe("/_form.html.jade") {
    it("should be created as expected") {
      val code = generator.formHtmlCode(Seq("admin"), "members", "member", Seq(
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
          |-# Be aware of package imports.
          |-# 1. src/main/scala/templates/ScalatePackage.scala
          |-# 2. scalateTemplateConfig in project/Build.scala
          |
          |div(class="form-group")
          |  label(class="control-label" for="name") #{s.i18n.getOrKey("adminMember.name")}
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
          |  label(class="control-label" for="favorite_number") #{s.i18n.getOrKey("adminMember.favoriteNumber")}
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
          |  label(class="control-label" for="magic_number") #{s.i18n.getOrKey("adminMember.magicNumber")}
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
          |  label(class="control-label" for="is_activated") #{s.i18n.getOrKey("adminMember.isActivated")}
          |  div(class="controls row")
          |    div(class="col-xs-12")
          |      input(type="checkbox" name="is_activated" class="form-control" value="true" checked={s.params.is_activated==Some(true)})
          |div(class="form-group")
          |  label(class="control-label") #{s.i18n.getOrKey("adminMember.birthday")}
          |  div(class="controls row")
          |    div(class={if(keyAndErrorMessages.hasErrors("birthday")) "has-error" else ""})
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_year"   class="form-control" value={s.params.birthday_year}   placeholder={s.i18n.getOrKey("year")}   maxlength=4)
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_month"  class="form-control" value={s.params.birthday_month}  placeholder={s.i18n.getOrKey("month")}  maxlength=2)
          |      div(class="col-xs-2")
          |        input(type="text" name="birthday_day"    class="form-control" value={s.params.birthday_day}    placeholder={s.i18n.getOrKey("day")}    maxlength=2)
          |    - keyAndErrorMessages.get("birthday").map { errors =>
          |      div(class="col-xs-12 has-error")
          |        - for (error <- errors)
          |          label(class="control-label") #{error}
          |    - }
          |div(class="form-actions")
          |  =unescape(s.csrfHiddenInputTag)
          |  input(type="submit" class="btn btn-primary" value={s.i18n.getOrKey("submit")})
          |    a(class="btn btn-default" href={s.url(Controllers.adminMembers.indexUrl)}) #{s.i18n.getOrKey("cancel")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/new.html.jade") {
    it("should be created as expected") {
      val code = generator.newHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-@val s: skinny.Skinny
          |
          |-# Be aware of package imports.
          |-# 1. src/main/scala/templates/ScalatePackage.scala
          |-# 2. scalateTemplateConfig in project/Build.scala
          |
          |h3 #{s.i18n.getOrKey("adminMember.new")}
          |hr
          |
          |-#-for (e <- s.errorMessages)
          |-#  p(class="alert alert-danger") #{e}
          |
          |form(method="post" action={s.url(Controllers.adminMembers.createUrl)} class="form")
          |  =include("_form.html.jade")
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/edit.html.jade") {
    it("should be created as expected") {
      val code = generator.editHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-@val s: skinny.Skinny
          |
          |-# Be aware of package imports.
          |-# 1. src/main/scala/templates/ScalatePackage.scala
          |-# 2. scalateTemplateConfig in project/Build.scala
          |
          |h3 #{s.i18n.getOrKey("adminMember.edit")} : ##{s.params.id}
          |hr
          |
          |-#-for (e <- s.errorMessages)
          |-#  p(class="alert alert-danger") #{e}
          |
          |form(method="post" action={s.url(Controllers.adminMembers.updateUrl, "id" -> s.params.id)} class="form")
          |  =include("_form.html.jade")
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/index.html.jade") {
    it("should be created as expected") {
      val code = generator.indexHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-@val s: skinny.Skinny
          |-@val items: Seq[model.admin.Member]
          |-@val totalPages: Int
          |-@val page: Int = s.params.page.map(_.toString.toInt).getOrElse(1)
          |
          |-# Be aware of package imports.
          |-# 1. src/main/scala/templates/ScalatePackage.scala
          |-# 2. scalateTemplateConfig in project/Build.scala
          |
          |h3 #{s.i18n.getOrKey("adminMember.list")}
          |hr
          |-for (notice <- s.flash.notice)
          |  p(class="alert alert-info") #{notice}
          |
          |- if (totalPages > 1)
          |  ul.pagination
          |    li
          |      a(href={s.url(Controllers.adminMembers.indexUrl, "page" -> 1)}) &laquo;
          |    -val maxPage = Math.min(totalPages, if (page <= 5) 11 else page + 5)
          |    -for (i <- Math.max(1, maxPage - 10) to maxPage)
          |      li(class={if (i == page) "active" else ""})
          |        a(href={s.url(Controllers.adminMembers.indexUrl, "page" -> i)}) #{i}
          |    li
          |      a(href={s.url(Controllers.adminMembers.indexUrl, "page" -> totalPages)}) &raquo;
          |    li
          |      span #{Math.min(page, totalPages)} / #{totalPages}
          |
          |p(class="pull-right")
          |  a(href={s.url(Controllers.adminMembers.newUrl)} class="btn btn-primary") #{s.i18n.getOrKey("new")}
          |
          |table(class="table table-bordered")
          |  thead
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.id")}
          |      th #{s.i18n.getOrKey("adminMember.name")}
          |      th #{s.i18n.getOrKey("adminMember.favoriteNumber")}
          |      th #{s.i18n.getOrKey("adminMember.magicNumber")}
          |      th #{s.i18n.getOrKey("adminMember.isActivated")}
          |      th #{s.i18n.getOrKey("adminMember.birthday")}
          |      th
          |  tbody
          |  -for (item <- items)
          |    tr
          |      td #{item.id}
          |      td #{item.name}
          |      td #{item.favoriteNumber}
          |      td #{item.magicNumber}
          |      td #{item.isActivated}
          |      td #{item.birthday}
          |      td
          |        a(href={s.url(Controllers.adminMembers.showUrl, "id" -> item.id)} class="btn btn-default") #{s.i18n.getOrKey("detail")}
          |        a(href={s.url(Controllers.adminMembers.editUrl, "id" -> item.id)} class="btn btn-info") #{s.i18n.getOrKey("edit")}
          |        a(data-method="delete" data-confirm={s.i18n.getOrKey("adminMember.delete.confirm")} href={s.url(Controllers.adminMembers.destroyUrl, "id" -> item.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.getOrKey("delete")}
          |  -if (items.isEmpty)
          |    tr
          |      td(colspan="7") #{s.i18n.getOrKey("empty")}
          |
          |a(href={s.url(Controllers.adminMembers.newUrl)} class="btn btn-primary") #{s.i18n.getOrKey("new")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/show.html.jade") {
    it("should be created as expected") {
      val code = generator.showHtmlCode(Seq("admin"), "members", "member", Seq(
        "name" -> "String",
        "favoriteNumber" -> "Long",
        "magicNumber" -> "Option[Int]",
        "isActivated" -> "Boolean",
        "birthday" -> "Option[LocalDate]"
      ))

      val expected =
        """-@val item: model.admin.Member
          |-@val s: skinny.Skinny
          |
          |-# Be aware of package imports.
          |-# 1. src/main/scala/templates/ScalatePackage.scala
          |-# 2. scalateTemplateConfig in project/Build.scala
          |
          |h3 #{s.i18n.getOrKey("adminMember.detail")}
          |hr
          |-for (notice <- s.flash.notice)
          |  p(class="alert alert-info") #{notice}
          |table(class="table table-bordered")
          |  thead
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.id")}
          |      td #{item.id}
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.name")}
          |      td #{item.name}
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.favoriteNumber")}
          |      td #{item.favoriteNumber}
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.magicNumber")}
          |      td #{item.magicNumber}
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.isActivated")}
          |      td #{item.isActivated}
          |    tr
          |      th #{s.i18n.getOrKey("adminMember.birthday")}
          |      td #{item.birthday}
          |
          |hr
          |div(class="form-actions")
          |  a(class="btn btn-default" href={s.url(Controllers.adminMembers.indexUrl)}) #{s.i18n.getOrKey("backToList")}
          |  a(href={s.url(Controllers.adminMembers.editUrl, "id" -> item.id)} class="btn btn-info") #{s.i18n.getOrKey("edit")}
          |  a(data-method="delete" data-confirm={s.i18n.getOrKey("adminMember.delete.confirm")} href={s.url(Controllers.adminMembers.destroyUrl, "id" -> item.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.getOrKey("delete")}
          |""".stripMargin
      code should equal(expected)
    }
  }
}
