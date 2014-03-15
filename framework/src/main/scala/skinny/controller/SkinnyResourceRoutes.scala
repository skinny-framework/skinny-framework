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
  // create API
  val createApiUrl = post(s"${resourcesBasePath}.:ext")(createApiAction).as('createApi)
  @deprecated("Use createApiUrl instead", since = "1.0.0")
  val createExtUrl = createApiUrl

  def createApiAction = params.get("ext").map {
    case "json" => createResource()(Format.JSON)
    case "xml" => createResource()(Format.XML)
    case _ => haltWithBody(404)
  } getOrElse haltWithBody(404)

  // --------------
  // index API

  val indexApiUrl: Route = get(s"${resourcesBasePath}.:ext")(indexApiAction).as('index)
  @deprecated("Use indexApiUrl instead", since = "1.0.0")
  val indexExtUrl = indexApiUrl

  protected def indexApiAction = (for {
    ext <- params.get("ext")
  } yield {
    ext match {
      case "json" => showResources()(Format.JSON)
      case "xml" => showResources()(Format.XML)
      case _ => haltWithBody(404)
    }
  }) getOrElse haltWithBody(404)

  // --------------
  // show API

  val showApiUrl: Route = routeForShowApi
  @deprecated("Use showApiUrl instead", since = "1.0.0")
  val showExtUrl = showApiUrl

  protected def routeForShowApi = {
    get(s"${resourcesBasePath}/:${idParamName}.:ext")(showApiAction).as('showApi)
  }
  protected def showApiAction = (for {
    id <- params.getAs[Id](idParamName)
    ext <- params.get("ext")
  } yield {
    ext match {
      case "json" => showResource(id)(Format.JSON)
      case "xml" => showResource(id)(Format.XML)
      case _ => haltWithBody(404)
    }
  }) getOrElse haltWithBody(404)

  // --------------
  // update API

  val updatePostApiUrl = post(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateApi)
  @deprecated("Use updatePostApiUrl instead", since = "1.0.0")
  val updatePostExtUrl = updatePostApiUrl

  val updatePutApiUrl = put(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateApi)
  @deprecated("Use updatePutApiUrl instead", since = "1.0.0")
  val updatePutExtUrl = updatePutApiUrl

  val updatePatchApiUrl = patch(s"${resourcesBasePath}/:${idParamName}.:ext")(updateApiAction).as('updateApi)
  @deprecated("Use updatePatchApiUrl instead", since = "1.0.0")
  val updatePatchExtUrl = updatePatchApiUrl

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
  // delete API

  val destroyApiUrl = delete(s"${resourcesBasePath}/:${idParamName}.:ext")(deleteApiAction).as('destroyApi)
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
  val indexWithSlashUrl = get(s"${resourcesBasePath}/")(showResources()).as('index)

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

  val createUrl = post(s"${resourcesBasePath}")(createResource).as('create)
  val createWithSlashUrl = post(s"${resourcesBasePath}/")(createResource).as('create)

  // --------------
  // update

  val editUrl = get(s"${resourcesBasePath}/:${idParamName}/edit") {
    params.getAs[Id](idParamName).map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as('edit)

  val updatePostUrl = post(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)
  val updateUrl = put(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)
  val updatePatchUrl = patch(s"${resourcesBasePath}/:${idParamName}")(updateAction).as('update)

  protected def updateAction = {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }

  // --------------
  // delete

  val destroyUrl = delete(s"${resourcesBasePath}/:${idParamName}")(deleteAction).as('destroy)
  @deprecated("Use destroyUrl instead", since = "1.0.0")
  val deleteUrl = destroyUrl

  protected def deleteAction = {
    params.getAs[Id](idParamName).map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }

}
