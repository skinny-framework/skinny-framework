package skinny.controller

import org.scalatra.util.conversion.TypeConverter
import skinny._
import org.scalatra.Route

/**
 * Routes for Skinny API resource.
 */
trait SkinnyApiResourceRoutes[Id] extends SkinnyController with Routes { self: SkinnyResourceActions[Id] =>

  /**
   * to enable params.getAs[Id]("id")
   */
  implicit val scalatraParamsIdTypeConverter: TypeConverter[String, Id] = new TypeConverter[String, Id] {
    def apply(s: String): Option[Id] = Option(s).map(_.asInstanceOf[Id])
  }

  /**
   * Pass this controller instance implicitly
   * because [[skinny.routing.implicits.RoutesAsImplicits]] expects [[skinny.controller.SkinnyControllerBase]].
   */
  private[this] implicit val skinnyController: SkinnyController = this

  // --------------
  // create

  val createUrl = post(s"${resourcesBasePath}")(createResource).as('create)
  val createWithSlashUrl = post(s"${resourcesBasePath}/")(createResource).as('createWithSlash)
  val createExtUrl = post(s"${resourcesBasePath}.:ext") {
    params.get("ext").map {
      case "json" => createResource()(Format.JSON)
      case "xml" => createResource()(Format.XML)
      case _ => haltWithBody(404)
    } getOrElse haltWithBody(404)
  }.as('createWithExt)

  // --------------
  // show

  val indexExtUrl: Route = routeForIndexApi

  protected def routeForIndexApi = get(s"${resourcesBasePath}.:ext") {
    (for {
      ext <- params.get("ext")
    } yield {
      ext match {
        case "json" => showResources()(Format.JSON)
        case "xml" => showResources()(Format.XML)
        case _ => haltWithBody(404)
      }
    }) getOrElse haltWithBody(404)
  }.as('index)

  val showApiUrl: Route = routeForShowApi
  @deprecated("Use showApiUrl instead", since = "1.0.0")
  val showExtUrl = showApiUrl

  protected def routeForShowApi = get(s"${resourcesBasePath}/:${idParamName}.:ext") {
    (for {
      id <- params.getAs[Id](idParamName)
      ext <- params.get("ext")
    } yield {
      ext match {
        case "json" => showResource(id)(Format.JSON)
        case "xml" => showResource(id)(Format.XML)
        case _ => haltWithBody(404)
      }
    }) getOrElse haltWithBody(404)
  }.as('show)

  // --------------
  // update

  val updatePostUrl = post(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)
  val updatePostApiUrl = post(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateWithExt)
  @deprecated("Use updatePostApiUrl instead", since = "1.0.0")
  val updatePostExtUrl = updatePostApiUrl

  val updateUrl = put(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)
  val updatePutApiUrl = put(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateWithExt)
  @deprecated("Use updatePutApiUrl instead", since = "1.0.0")
  val updatePutExtUrl = updatePutApiUrl

  val updatePatchUrl = patch(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)
  val updatePatchApiUrl = patch(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateWithExt)
  @deprecated("Use updatePatchApiUrl instead", since = "1.0.0")
  val updatePatchExtUrl = updatePatchApiUrl

  protected def updateAction = {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }

  protected def updateApiAction = {
    (for {
      id <- params.getAs[Id](idParamName)
      ext <- params.get("ext")
    } yield {
      ext match {
        case "json" => updateResource(id)(Format.JSON)
        case "xml" => updateResource(id)(Format.XML)
        case _ => haltWithBody(404)
      }
    }) getOrElse haltWithBody(404)
  }

  // --------------
  // delete

  val destroyUrl = delete(s"${resourcesBasePath}/:${idParamName}")(deleteAction).as('destroy)
  @deprecated("Use destroyUrl instead", since = "1.0.0")
  val deleteUrl = destroyUrl

  protected def deleteAction = {
    params.getAs[Id](idParamName).map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }

  val destroyApiUrl = delete(s"${resourcesBasePath}/:${idParamName}.:ext")(deleteApiAction).as('destroyWithExt)
  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val destroyExtUrl = destroyApiUrl
  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val deleteApiUrl = destroyApiUrl
  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val deleteExtUrl = destroyApiUrl

  protected def deleteApiAction = {
    (for {
      id <- params.getAs[Id](idParamName)
      ext <- params.get("ext")
    } yield {
      ext match {
        case "json" => destroyResource(id)(Format.JSON)
        case "xml" => destroyResource(id)(Format.XML)
        case _ => haltWithBody(404)
      }
    }) getOrElse haltWithBody(404)
  }

}

/**
 * Routes for Skinny resource.
 */
trait SkinnyResourceRoutes[Id] extends SkinnyApiResourceRoutes[Id] { self: SkinnyResourceActions[Id] =>

  // --------------
  // show

  val indexUrl = get(s"${resourcesBasePath}")(showResources()).as('index)
  val indexWithSlashUrl = get(s"${resourcesBasePath}/")(showResources()).as('indexWithSlash)

  val showUrl = get(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => showResource(id)).getOrElse(haltWithBody(404))
  }.as('show)

  // Scalatra takes priority to route definition which is defined later.
  // So showExtUrl is defined again here.
  override val showApiUrl = routeForShowApi
  override val showExtUrl = routeForShowApi

  // --------------
  // create
  // Scalatra takes priority to route definition which is defined later.
  val newUrl = get(s"${resourcesBasePath}/new")(newResource).as('new)

  // --------------
  // update

  val editUrl = get(s"${resourcesBasePath}/:${idParamName}/edit") {
    params.getAs[Id](idParamName).map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as('edit)

}
