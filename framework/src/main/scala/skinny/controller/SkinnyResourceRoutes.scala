package skinny.controller

import skinny.routing.Routes
import skinny.routing.implicits.RoutesAsImplicits

/**
  * Routes for Skinny resource.
  */
trait SkinnyResourceRoutes[Id] extends SkinnyApiResourceRoutes[Id] with Routes { self: SkinnyResourceActions[Id] =>

  // --------------
  // show

  val indexUrl          = get(s"${resourcesBasePath}")(showResources()).as(Symbol("index"))
  val indexWithSlashUrl = get(s"${resourcesBasePath}/")(showResources()).as(Symbol("index"))

  val showUrl = get(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => showResource(id)).getOrElse(haltWithBody(404))
  }.as(Symbol("show"))

  // Scalatra takes priority to route definition which is defined later.
  // So showExtUrl is defined again here.
  override val showApiUrl = routeForShowApi

  // --------------
  // create
  // Scalatra takes priority to route definition which is defined later.
  val newUrl = get(s"${resourcesBasePath}/new")(newResource).as(Symbol("new"))

  val createUrl          = post(s"${resourcesBasePath}")(createResource).as(Symbol("create"))
  val createWithSlashUrl = post(s"${resourcesBasePath}/")(createResource).as(Symbol("create"))

  // --------------
  // update

  val editUrl = get(s"${resourcesBasePath}/:${idParamName}/edit") {
    params.getAs[Id](idParamName).map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as(Symbol("edit"))

  val updatePostUrl  = post(s"${resourcesBasePath}/:${idParamName}")(updateAction).as(Symbol("update"))
  val updateUrl      = put(s"${resourcesBasePath}/:${idParamName}")(updateAction).as(Symbol("update"))
  val updatePatchUrl = patch(s"${resourcesBasePath}/:${idParamName}")(updateAction).as(Symbol("update"))

  protected def updateAction = {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }

  // --------------
  // delete

  val destroyUrl = delete(s"${resourcesBasePath}/:${idParamName}")(deleteAction).as(Symbol("destroy"))

  protected def deleteAction = {
    params.getAs[Id](idParamName).map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }

  // ###########################
  // override API routes
  post(s"${resourcesBasePath}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    createApiAction
  }).as(Symbol("createApi"))

  post(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as(Symbol("updateApi"))
  put(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as(Symbol("updateApi"))
  patch(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as(Symbol("updateApi"))

  delete(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    deleteApiAction
  }).as(Symbol("destroyApi"))
  // ###########################

}
