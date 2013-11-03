package skinny.task.generator

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class ScaffoldScamlGeneratorSpec extends FunSpec with ShouldMatchers {

  val generator = ScaffoldScamlGenerator

  describe("/new.html.scaml") {
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
          |%h3 #{s.i18n.get("member.new")}
          |%hr
          |
          |-for (e <- s.errorMessages)
          | %p(class="alert alert-danger") #{e}
          |
          |%form(method="post" action={uri("/members")} class="form")
          | %div(class="form-group")
          |  %label(class="control-label" for="name") #{s.i18n.get("member.name")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="name" class="input-lg col-lg-6" value={s.params.name})
          | %div(class="form-group")
          |  %label(class="control-label" for="favoriteNumber") #{s.i18n.get("member.favoriteNumber")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="favoriteNumber" class="input-lg col-lg-6" value={s.params.favoriteNumber})
          | %div(class="form-group")
          |  %label(class="control-label" for="magicNumber") #{s.i18n.get("member.magicNumber")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="magicNumber" class="input-lg col-lg-6" value={s.params.magicNumber})
          | %div(class="form-group")
          |  %label(class="control-label" for="isActivated") #{s.i18n.get("member.isActivated")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="checkbox" name="isActivated" value="true" checked={s.params.isActivated==Some(true)})
          | %div(class="form-group")
          |  %label(class="control-label" for="birthday") #{s.i18n.get("member.birthday")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="birthday" class="input-lg col-lg-6" value={s.params.birthday})
          |
          | != s.csrfHiddenInputTag
          | %div(class="form-actions")
          |  %input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
          |  %a(class="btn btn-default" href={uri("/members")}) #{s.i18n.get("cancel")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/edit.html.scaml") {
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
          |%h3 #{s.i18n.get("member.edit")}
          |%hr
          |
          |-for (e <- s.errorMessages)
          | %p(class="alert alert-danger") #{e}
          |
          |%form(method="post" action={uri("/members/" + s.params.id.get)} class="form")
          | %div(class="form-group")
          |  %label(class="control-label" for="name") #{s.i18n.get("member.name")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="name" class="input-lg col-lg-6" value={s.params.name})
          | %div(class="form-group")
          |  %label(class="control-label" for="favoriteNumber") #{s.i18n.get("member.favoriteNumber")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="favoriteNumber" class="input-lg col-lg-6" value={s.params.favoriteNumber})
          | %div(class="form-group")
          |  %label(class="control-label" for="magicNumber") #{s.i18n.get("member.magicNumber")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="magicNumber" class="input-lg col-lg-6" value={s.params.magicNumber})
          | %div(class="form-group")
          |  %label(class="control-label" for="isActivated") #{s.i18n.get("member.isActivated")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="checkbox" name="isActivated" value="true" checked={s.params.isActivated==Some(true)})
          | %div(class="form-group")
          |  %label(class="control-label" for="birthday") #{s.i18n.get("member.birthday")}
          |  %div(class="controls")
          |   %div(class="row col-md-12")
          |    %input(type="text" name="birthday" class="input-lg col-lg-6" value={s.params.birthday})
          |
          | != s.csrfHiddenInputTag
          | %div(class="form-actions")
          |  %input(type="submit" class="btn btn-primary" value={s.i18n.get("submit")})
          |  %a(class="btn btn-default" href={uri("/members")}) #{s.i18n.get("cancel")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/index.html.scaml") {
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
          |
          |%h3 #{s.i18n.get("member.list")}
          |%hr
          |-for (notice <- s.flash.notice)
          | %p(class="alert alert-info") #{notice}
          |
          |%table(class="table table-bordered")
          | %thead
          |  %tr
          |   %th #{s.i18n.get("member.id")}
          |   %th #{s.i18n.get("member.name")}
          |   %th #{s.i18n.get("member.favoriteNumber")}
          |   %th #{s.i18n.get("member.magicNumber")}
          |   %th #{s.i18n.get("member.isActivated")}
          |   %th #{s.i18n.get("member.birthday")}
          |   %th
          | %tbody
          | -for (member <- members)
          |  %tr
          |   %td #{member.id}
          |   %td #{member.name}
          |   %td #{member.favoriteNumber}
          |   %td #{member.magicNumber}
          |   %td #{member.isActivated}
          |   %td #{member.birthday}
          |   %td
          |    %a(href={uri("/members/" + member.id)} class="btn btn-default") #{s.i18n.get("detail")}
          |    %a(href={uri("/members/" + member.id + "/edit")} class="btn btn-info") #{s.i18n.get("edit")}
          |    %a(data-method="delete" data-confirm={s.i18n.get("member.delete.confirm")} href={uri("/members/" + member.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
          |
          |%a(href={uri("/members/new")} class="btn btn-primary") #{s.i18n.get("new")}
          |""".stripMargin
      code should equal(expected)
    }
  }

  describe("/show.html.scaml") {
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
          |%h3 #{s.i18n.get("member.detail")}
          |%hr
          |-for (notice <- s.flash.notice)
          | %p(class="alert alert-info") #{notice}
          |%table(class="table table-bordered")
          | %thead
          |  %tr
          |   %th #{s.i18n.get("member.id")}
          |   %td #{member.id}
          |  %tr
          |   %th #{s.i18n.get("member.name")}
          |   %td #{member.name}
          |  %tr
          |   %th #{s.i18n.get("member.favoriteNumber")}
          |   %td #{member.favoriteNumber}
          |  %tr
          |   %th #{s.i18n.get("member.magicNumber")}
          |   %td #{member.magicNumber}
          |  %tr
          |   %th #{s.i18n.get("member.isActivated")}
          |   %td #{member.isActivated}
          |  %tr
          |   %th #{s.i18n.get("member.birthday")}
          |   %td #{member.birthday}
          |
          |%hr
          |%div(class="form-actions")
          | %a(class="btn btn-default" href={uri("/members")}) #{s.i18n.get("backToList")}
          | %a(href={uri("/members/" + member.id + "/edit")} class="btn btn-info") #{s.i18n.get("edit")}
          | %a(data-method="delete" data-confirm={s.i18n.get("member.delete.confirm")} href={uri("/members/" + member.id)} rel="nofollow" class="btn btn-danger") #{s.i18n.get("delete")}
          |""".stripMargin
      code should equal(expected)
    }
  }
}
