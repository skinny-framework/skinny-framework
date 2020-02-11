package skinny.controller

import skinny.routing.Routes
import skinny.routing.implicits.RoutesAsImplicits

/**
  * Routes for Skinny resource.
  */
trait SkinnyResourceRoutes[Id] extends SkinnyApiResourceRoutes[Id] with Routes { self: SkinnyResourceActions[Id] =>

  // --------------
  // show

  val indexUrl          = get(s"${resourcesBasePath}")(showResources()).as("index")
  val indexWithSlashUrl = get(s"${resourcesBasePath}/")(showResources()).as("index")

  val showUrl = get(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => showResource(id)).getOrElse(haltWithBody(404))
  }.as("show")

  // Scalatra takes priority to route definition which is defined later.
  // So showExtUrl is defined again here.
  override val showApiUrl = routeForShowApi

  // --------------
  // create
  // Scalatra takes priority to route definition which is defined later.
  val newUrl = get(s"${resourcesBasePath}/new")(newResource).as("new")

  val createUrl          = post(s"${resourcesBasePath}")(createResource).as("create")
  val createWithSlashUrl = post(s"${resourcesBasePath}/")(createResource).as("create")

  // --------------
  // update

  val editUrl = get(s"${resourcesBasePath}/:${idParamName}/edit") {
    params.getAs[Id](idParamName).map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as("edit")

  val updatePostUrl  = post(s"${resourcesBasePath}/:${idParamName}")(updateAction).as("update")
  val updateUrl      = put(s"${resourcesBasePath}/:${idParamName}")(updateAction).as("update")
  val updatePatchUrl = patch(s"${resourcesBasePath}/:${idParamName}")(updateAction).as("update")

  protected def updateAction = {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }

  // --------------
  // delete

  val destroyUrl = delete(s"${resourcesBasePath}/:${idParamName}")(deleteAction).as("destroy")

  protected def deleteAction = {
    params.getAs[Id](idParamName).map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }

  // ###########################
  // override API routes
  post(s"${resourcesBasePath}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    createApiAction
  }).as("createApi")

  post(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as("updateApi")
  put(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as("updateApi")
  patch(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    updateApiAction
  }).as("updateApi")

  delete(s"${resourcesBasePath}/:${idParamName}.:ext")({
    setContentTypeFromSkinnyApiResourceExtParam
    deleteApiAction
  }).as("destroyApi")
  // ###########################

}
