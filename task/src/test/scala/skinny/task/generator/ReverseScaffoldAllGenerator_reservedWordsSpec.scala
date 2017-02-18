package skinny.task.generator

import java.io.File
import java.nio.charset.Charset

import org.apache.commons.io.FileUtils
import org.scalatest._
import scalikejdbc._
import skinny.{ DBSettings, SkinnyEnv }

class ReverseScaffoldAllGenerator_reservedWordsSpec extends FunSpec with Matchers {

  val generator = new ReverseScaffoldAllGenerator {
    override def sourceDir = "tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala"
    override def resourceDir = "tmp/ReverseScaffoldAllGeneratorSpec/src/main/resources"
    override def testSourceDir = "tmp/ReverseScaffoldAllGeneratorSpec/src/test/scala"
    override def testResourceDir = "tmp/ReverseScaffoldAllGeneratorSpec/src/test/resources"
    override def webInfDir = "tmp/ReverseScaffoldAllGeneratorSpec/src/main/webapp/WEB-INF"
  }

  describe("ReverseScaffoldAllGenerator") {
    it("should be created as expected") {
      System.setProperty(SkinnyEnv.PropertyKey, "test")
      DBSettings.initialize()
      DB.localTx { implicit s =>
        sql"""
drop table user_group if exists;
drop table user_groups if exists;
create table user_groups (
  id bigserial not null primary key,
  name varchar(100) not null,
  type varchar(512)
);

drop table organization if exists;
drop table organizations if exists;
create table organizations (
  id bigserial not null primary key,
  name varchar(100) not null,
  type varchar(512) not null
);

drop table developer if exists;
drop table developers if exists;
create table developers (
  id bigserial not null primary key,
  name varchar(512) not null,
  nickname varchar(32),
  user_group_id bigint references user_groups(id)
);

drop table organization_developer if exists;
drop table organization_developers if exists;
drop table organizations_developers if exists;
create table organization_developers (
  organization_id bigint not null references organizations(id),
  developer_id bigint not null references developers(id)
);
""".execute.apply()
      }

      FileUtils.deleteDirectory(new File("tmp/ReverseScaffoldAllGeneratorSpec"))
      generator.run("ssp", List(), SkinnyEnv.get())

      val developer = FileUtils.readFileToString(
        new File("tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala/model/Developer.scala"),
        Charset.defaultCharset()
      )
      developer should equal(
        s"""package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Developer(
  id: Long,
  name: String,
  nickname: Option[String] = None,
  userGroupId: Option[Long] = None,
  userGroup: Option[UserGroup] = None,
  organizations: Seq[Organization] = Nil
)

object Developer extends SkinnyCRUDMapper[Developer] {
  override lazy val tableName = "developers"
  override lazy val defaultAlias = createAlias("d")

  lazy val userGroupRef = belongsTo[UserGroup](UserGroup, (d, ug) => d.copy(userGroup = ug))

  lazy val organizationsRef = hasManyThrough[Organization](
    through = OrganizationDeveloper,
    many = Organization,
    merge = (d, os) => d.copy(organizations = os)
  )

  override def extract(rs: WrappedResultSet, rn: ResultName[Developer]): Developer = {
    autoConstruct(rs, rn, "userGroup", "organizations")
  }
}
"""
      )
      val organization = FileUtils.readFileToString(
        new File("tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala/model/Organization.scala"),
        Charset.defaultCharset()
      )
      organization should equal(
        s"""package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class Organization(
  id: Long,
  name: String,
  theType: String,
  developers: Seq[Developer] = Nil
)

object Organization extends SkinnyCRUDMapper[Organization] {
  override lazy val tableName = "organizations"
  override lazy val defaultAlias = createAlias("o")
  override lazy val nameConverters = Map("^theType$$" -> "type")

  lazy val developersRef = hasManyThrough[Developer](
    through = OrganizationDeveloper,
    many = Developer,
    merge = (o, ds) => o.copy(developers = ds)
  )

  override def extract(rs: WrappedResultSet, rn: ResultName[Organization]): Organization = {
    autoConstruct(rs, rn, "developers")
  }
}
"""
      )
      val organizationDeveloper = FileUtils.readFileToString(
        new File("tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala/model/OrganizationDeveloper.scala"),
        Charset.defaultCharset()
      )
      organizationDeveloper should equal(
        s"""package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

case class OrganizationDeveloper(
  organizationId: Long,
  developerId: Long,
  developer: Option[Developer] = None,
  organization: Option[Organization] = None
)

object OrganizationDeveloper extends SkinnyNoIdCRUDMapper[OrganizationDeveloper] {
  override lazy val tableName = "organization_developers"
  override lazy val defaultAlias = createAlias("od")

  lazy val developerRef = belongsTo[Developer](Developer, (od, d) => od.copy(developer = d))

  lazy val organizationRef = belongsTo[Organization](Organization, (od, o) => od.copy(organization = o))

  override def extract(rs: WrappedResultSet, rn: ResultName[OrganizationDeveloper]): OrganizationDeveloper = {
    autoConstruct(rs, rn, "developer", "organization")
  }
}
"""
      )
      val developersController = FileUtils.readFileToString(
        new File("tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala/controller/DevelopersController.scala"),
        Charset.defaultCharset()
      )
      developersController should equal(
        """package controller

import skinny._
import skinny.validator._
import _root_.controller._
import model.Developer

class DevelopersController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Developer
  override def resourcesName = "developers"
  override def resourceName = "developer"

  override def resourcesBasePath = s"/${toSnakeCase(resourcesName)}"
  override def useSnakeCasedParamKeys = true

  override def viewsDirectoryPath = s"/${resourcesName}"

  override def createParams = Params(params)
  override def createForm = validation(createParams,
    paramKey("name") is required & maxLength(512),
    paramKey("nickname") is maxLength(32),
    paramKey("user_group_id") is numeric & longValue
  )
  override def createFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "nickname" -> ParamType.String,
    "user_group_id" -> ParamType.Long
  )

  override def updateParams = Params(params)
  override def updateForm = validation(updateParams,
    paramKey("name") is required & maxLength(512),
    paramKey("nickname") is maxLength(32),
    paramKey("user_group_id") is numeric & longValue
  )
  override def updateFormStrongParameters = Seq(
    "name" -> ParamType.String,
    "nickname" -> ParamType.String,
    "user_group_id" -> ParamType.Long
  )

}
"""
      )
      val indexHtmlSsp = FileUtils.readFileToString(
        new File("tmp/ReverseScaffoldAllGeneratorSpec/src/main/webapp/WEB-INF/views/developers/index.html.ssp"),
        Charset.defaultCharset()
      )
      indexHtmlSsp should equal(
        """<%@val s: skinny.Skinny %>
<%@val items: Seq[model.Developer] %>
<%@val totalPages: Int %>
<%@val page: Int = s.params.page.map(_.toString.toInt).getOrElse(1) %>

<%-- Be aware of package imports.
 1. tmp/ReverseScaffoldAllGeneratorSpec/src/main/scala/templates/ScalatePackage.scala
 2. scalateTemplateConfig in project/Build.scala
--%>

<h3>${s.i18n.getOrKey("developer.list")}</h3>
<hr/>
#for (notice <- s.flash.notice)
  <p class="alert alert-info">${notice}</p>
#end

#if (totalPages > 1)
  <ul class="pagination">
    <li>
      <a href="${s.url(Controllers.developers.indexUrl, "page" -> 1)}">&laquo;</a>
    </li>
    <% val maxPage = Math.min(totalPages, if (page <= 5) 11 else page + 5) %>
    #for (i <- Math.max(1, maxPage - 10) to maxPage)
      <li class="${if (i == page) "active" else ""}">
        <a href="${s.url(Controllers.developers.indexUrl, "page" -> i)}">${i}</a>
      </li>
    #end
    <li>
      <a href="${s.url(Controllers.developers.indexUrl, "page" -> totalPages)}">&raquo;</a>
    </li>
    <li>
      <span>${Math.min(page, totalPages)} / ${totalPages}</span>
    </li>
  </ul>
#end

<div class="pull-right">
  <a href="${s.url(Controllers.developers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
</div>

<table class="table table-bordered">
<thead>
  <tr>
    <th>${s.i18n.getOrKey("developer.id")}</th>
    <th>${s.i18n.getOrKey("developer.name")}</th>
    <th>${s.i18n.getOrKey("developer.nickname")}</th>
    <th>${s.i18n.getOrKey("developer.userGroupId")}</th>
    <th></th>
  </tr>
</thead>
<tbody>
  #for (item <- items)
  <tr>
    <td>${item.id}</td>
    <td>${item.name}</td>
    <td>${item.nickname}</td>
    <td>${item.userGroupId}</td>
    <td>
      <a href="${s.url(Controllers.developers.showUrl, "id" -> item.id)}" class="btn btn-default">${s.i18n.getOrKey("detail")}</a>
      <a href="${s.url(Controllers.developers.editUrl, "id" -> item.id)}" class="btn btn-info">${s.i18n.getOrKey("edit")}</a>
      <a data-method="delete" data-confirm="${s.i18n.getOrKey("developer.delete.confirm")}"
        href="${s.url(Controllers.developers.destroyUrl, "id" -> item.id)}" rel="nofollow" class="btn btn-danger">${s.i18n.getOrKey("delete")}</a>
    </td>
  </tr>
  #end
  #if (items.isEmpty)
  <tr>
    <td colspan="5">${s.i18n.getOrKey("empty")}</td>
  </tr>
  #end
</tbody>
</table>

<a href="${s.url(Controllers.developers.newUrl)}" class="btn btn-primary">${s.i18n.getOrKey("new")}</a>
"""
      )
    }
  }

}
