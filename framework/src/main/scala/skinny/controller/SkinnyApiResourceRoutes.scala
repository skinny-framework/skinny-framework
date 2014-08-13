package skinny.controller

import org.scalatra.Route
import org.scalatra.util.conversion.TypeConverter
import skinny._

/**
 * Routes for Skinny API resource.
 */
trait SkinnyApiResourceRoutes[Id] extends SkinnyControllerBase with Routes { self: SkinnyApiResourceActions[Id] =>

  /**
   * Set Content-Type response header which is suitable for specified extension.
   */
  protected def setContentTypeFromSkinnyApiResourceExtParam: Unit = {
    params.get("ext").foreach(f => setContentTypeIfAbsent()(Format(f)))
  }

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
  private[this] implicit val skinnyController: SkinnyControllerBase = this

  // --------------
  // create API
  val createApiUrl = post(s"${resourcesBasePath}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    createApiAction
  }).as('createApi)

  @deprecated("Use createApiUrl instead", since = "1.0.0")
  val createExtUrl = createApiUrl

  protected def createApiAction = params.get("ext").map {
    case "json" => createResource()(Format.JSON)
    case "xml" => createResource()(Format.XML)
    case _ => haltWithBody(404)
  } getOrElse haltWithBody(404)

  // --------------
  // index API

  val indexApiUrl: Route = get(s"${resourcesBasePath}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    indexApiAction
  }).as('indexApi)

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
    get(s"${resourcesBasePath}/:${idParamName}.:ext")({
      setContentTypeFromSkinnyApiResourceExtParam
      showApiAction
    }).as('showApi)
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

  val updatePostApiUrl = post(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as('updateApi)

  @deprecated("Use updatePostApiUrl instead", since = "1.0.0")
  val updatePostExtUrl = updatePostApiUrl

  val updatePutApiUrl = put(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as('updateApi)

  @deprecated("Use updatePutApiUrl instead", since = "1.0.0")
  val updatePutExtUrl = updatePutApiUrl

  val updatePatchApiUrl = patch(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as('updateApi)

  @deprecated("Use updatePatchApiUrl instead", since = "1.0.0")
  val updatePatchExtUrl = updatePatchApiUrl

  protected def updateApiAction = {
    params.get("ext").map(ext => Format(ext)).map { implicit format =>
      format match {
        case Format.HTML => haltWithBody(404)
        case _ =>
          params.getAs[Id](idParamName) match {
            case Some(id) => updateResource(id)
            case _ => haltWithBody(404)
          }
      }
    }.getOrElse(haltWithBody(404))
  }

  // --------------
  // delete API

  val destroyApiUrl = delete(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    deleteApiAction
  }).as('destroyApi)

  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val destroyExtUrl = destroyApiUrl
  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val deleteApiUrl = destroyApiUrl
  @deprecated("Use destroyApiUrl instead", since = "1.0.0")
  val deleteExtUrl = destroyApiUrl

  protected def deleteApiAction = {
    params.get("ext").map(ext => Format(ext)).map { implicit format =>
      format match {
        case Format.HTML => haltWithBody(404)
        case _ =>
          params.getAs[Id](idParamName) match {
            case Some(id) => destroyResource(id)
            case _ => haltWithBody(404)
          }
      }
    }.getOrElse(haltWithBody(404))
  }

}
