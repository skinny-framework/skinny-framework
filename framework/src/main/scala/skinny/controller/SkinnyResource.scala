package skinny.controller

import skinny._
import skinny.ParamType
import skinny.validator.{ NewValidation, MapValidator }
import skinny.exception.StrongParametersException
import java.util.Locale
import org.scalatra.util.conversion.{ Conversions, TypeConverter }
import skinny.controller.feature.RequestScopeFeature

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 *
 * SkinnyResource is surely inspired by Rails ActiveSupport.
 */

trait SkinnyResource extends SkinnyResourceWithId[Long] {

  implicit override val scalatraParamsIdTypeConverter: TypeConverter[String, Long] = Conversions.stringToLong
}

trait SkinnyResourceWithId[Id]
  extends SkinnyController
  with SkinnyResourceActions[Id]
  with SkinnyResourceRoutes[Id]

/**
 * Actions for Skinny resource.
 */
trait SkinnyResourceActions[Id] { self: SkinnyController =>

  // set resourceName/resourcesName to the request scope
  beforeAction() {
    set(RequestScopeFeature.ATTR_RESOURCES_NAME -> itemsName)
    set(RequestScopeFeature.ATTR_RESOURCE_NAME -> itemName)
  }

  /**
   * SkinnyModel for this resource.
   */
  protected def model: SkinnyModel[Id, _]

  /**
   * Id field name.
   */
  protected def idName: String = "id"

  /**
   * Id parameter name.
   */
  protected def idParamName: String = toSnakeCase(idName)

  /**
   * Resource name in the plural. This name will be used for path and directory name to locale template files.
   */
  protected def resourcesName: String

  /**
   * Resource name in the singular. This name will be used for path and validator's prefix.
   */
  protected def resourceName: String

  /**
   * Items variable name in view templates.
   */
  protected def itemsName: String = "items"

  /**
   * Item variable name in view templates.
   */
  protected def itemName: String = "item"

  /**
   * Directory path which contains view templates under src/main/webapp/WEB-INF/views.
   */
  protected def viewsDirectoryPath: String = s"/${resourcesName}"

  /**
   * Root element name in the XML response.
   */
  override protected def xmlRootName = resourcesName

  /**
   * Each resource item element name in the XML response.
   */
  override protected def xmlItemName = resourceName

  /**
   * Creates validator with prefix(resourceName).
   *
   * @param params params
   * @param validations validations
   * @param locale current locale
   * @return validator
   */
  override def validation(params: Params, validations: NewValidation*)(
    implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {
    validationWithPrefix(params, resourceName, validations: _*)
  }

  /**
   * Use relative path if true. This is set as false by default.
   *
   * If you set this as true, routing will become simpler but /{resources}.xml or /{resources}.json don't work.
   */
  protected def useRelativePathForResourcesBasePath: Boolean = false

  /**
   * Base path prefix. (e.g. /admin/{resourcesName} )
   */
  protected def resourcesBasePathPrefix: String = ""

  /**
   * Base path.
   */
  protected def resourcesBasePath: String = {
    resourcesBasePathPrefix + (if (useRelativePathForResourcesBasePath) "" else s"/${resourcesName}")
  }

  /**
   * Normalized base path. This method should not be overridden.
   */
  protected final def normalizedResourcesBasePath: String = {
    resourcesBasePath.replaceFirst("^/", "").replaceFirst("/$", "")
  }

  /**
   * Outputs debug logging for passed parameters.
   *
   * @param form input form
   * @param id id if exists
   */
  protected def debugLoggingParameters(form: MapValidator, id: Option[Id] = None) = {
    val forId = id.map { id => s" for [id -> ${id}]" }.getOrElse("")
    val params = form.paramMap.map { case (name, value) => s"${name} -> '${value}'" }.mkString("[", ", ", "]")
    logger.debug(s"Parameters${forId}: ${params}")
  }

  /**
   * Outputs debug logging for permitted parameters.
   *
   * @param parameters permitted strong parameters
   * @param id id if exists
   */
  protected def debugLoggingPermittedParameters(parameters: PermittedStrongParameters, id: Option[Id] = None) = {
    val forId = id.map { id => s" for [id -> ${id}]" }.getOrElse("")
    val params = parameters.params.map { case (name, (v, t)) => s"${name} -> '${v}' as ${t}" }.mkString("[", ", ", "]")
    logger.debug(s"Permitted parameters${forId}: ${params}")
  }

  // ----------------------------
  //  Actions for this resource
  // ----------------------------

  protected def enablePagination: Boolean = true

  protected def pageSize: Int = 20

  protected def pageNoParamName: String = "page"

  protected def totalPagesAttributeName: String = "totalPages"

  /**
   * Shows a list of resource.
   *
   * GET /{resources}/
   * GET /{resources}/?pageNo=1&pageSize=10
   * GET /{resources}
   * GET /{resources}.xml
   * GET /{resources}.json
   *
   * @param format format
   * @return list of resource
   */
  def showResources()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    if (enablePagination) {
      val pageNo = params.getAs[Int](pageNoParamName).getOrElse(1)
      set(itemsName, model.findModels(pageSize, pageNo))
      val totalPages: Int = (model.countAllModels() / pageSize).toInt + {
        if (model.countAllModels() % pageSize == 0) 0 else 1
      }
      set(totalPagesAttributeName -> totalPages)
    } else {
      set(itemsName, model.findAllModels())
    }
    render(s"${viewsDirectoryPath}/index")
  }

  /**
   * Show single resource.
   *
   * GET /{resources}/{id}
   * GET /{resources}/{id}.xml
   * GET /{resources}/{id}.json
   *
   * @param id id
   * @param format format
   * @return single resource
   */
  def showResource(id: Id)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    set(itemName, model.findModel(id).getOrElse(haltWithBody(404)))
    render(s"${viewsDirectoryPath}/show")
  }

  /**
   * Shows input form for creation.
   *
   * GET /{resources}/new
   *
   * @param format format
   * @return input form
   */
  def newResource()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    render(s"${viewsDirectoryPath}/new")
  }

  /**
   * Input form for creation
   */
  protected def createForm: MapValidator

  /**
   * Params for creation.
   */
  protected def createParams: Params = Params(params)

  /**
   * Strong parameter definitions for creation form
   */
  protected def createFormStrongParameters: Seq[(String, ParamType)]

  /**
   * Executes resource creation.
   *
   * @param parameters permitted parameters
   * @return generated resource id
   */
  protected def doCreateAndReturnId(parameters: PermittedStrongParameters): Id = {
    debugLoggingPermittedParameters(parameters)
    model.createNewModel(parameters)
  }

  /**
   * Set notice flash message for successful completion of creation.
   */
  protected def setCreateCompletionFlash(): Unit = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.created").getOrElse(s"The ${resourceName} was created."))
  }

  /**
   * Creates new resource.
   *
   * POST /{resources}
   *
   * @param format format
   * @return created response if success
   */
  def createResource()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val id = if (!createFormStrongParameters.isEmpty) {
        val parameters = createParams.permit(createFormStrongParameters: _*)
        doCreateAndReturnId(parameters)
      } else {
        throw new StrongParametersException(
          "'createFormStrongParameters' or 'createFormTypedStrongParameters' must be defined.")
      }
      format match {
        case Format.HTML =>
          setCreateCompletionFlash()
          redirect(s"/${normalizedResourcesBasePath}/${model.idToRawValue(id)}")
        case _ =>
          status = 201
          response.setHeader("Location", s"${contextPath}/${resourcesName}/${model.idToRawValue(id)}")
      }
    } else {
      status = 400
      render(s"${viewsDirectoryPath}/new")
    }
  }

  /**
   * Shows input form for modification.
   *
   * GET /{resources}/{id}/edit
   *
   * @param id id
   * @param format format
   * @return input form
   */
  def editResource(id: Id)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    model.findModel(id).map { m =>
      status = 200
      format match {
        case Format.HTML =>
          setAsParams(m)
          render(s"${viewsDirectoryPath}/edit")
        case _ =>
      }
    } getOrElse haltWithBody(404)
  }

  /**
   * Input form for modification
   */
  protected def updateForm: MapValidator

  /**
   * Params for modification.
   */
  protected def updateParams: Params = Params(params)

  /**
   * Strong parameter definitions for modification form
   */
  protected def updateFormStrongParameters: Seq[(String, ParamType)]

  /**
   * Executes modification for the specified resource.
   *
   * @param id  id
   * @param parameters permitted parameters
   * @return count
   */
  protected def doUpdate(id: Id, parameters: PermittedStrongParameters): Int = {
    debugLoggingPermittedParameters(parameters, Some(id))
    model.updateModelById(id, parameters)
  }

  /**
   * Set notice flash message for successful completion of modification.
   */
  protected def setUpdateCompletionFlash() = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.updated").getOrElse(s"The ${resourceName} was updated."))
  }

  /**
   * Updates the specified single resource.
   *
   * PUT /{resources}/{id}
   *
   * @param id id
   * @param format format
   * @return result
   */
  def updateResource(id: Id)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    debugLoggingParameters(updateForm, Some(id))

    model.findModel(id).map { m =>
      if (updateForm.validate()) {
        if (!updateFormStrongParameters.isEmpty) {
          val parameters = updateParams.permit(updateFormStrongParameters: _*)
          doUpdate(id, parameters)
        } else {
          throw new StrongParametersException(
            "'updateFormStrongParameters' or 'updateFormTypedStrongParameters' must be defined.")
        }
        status = 200
        format match {
          case Format.HTML =>
            setUpdateCompletionFlash()
            set(itemName, model.findModel(id).getOrElse(haltWithBody(404)))
            redirect(s"/${normalizedResourcesBasePath}/${model.idToRawValue(id)}")
          case _ =>
        }
      } else {
        status = 400
        render(s"${viewsDirectoryPath}/edit")
      }
    } getOrElse haltWithBody(404)
  }

  /**
   * Executes deletion of the specified single resource.
   *
   * @param id id
   * @return count
   */
  protected def doDestroy(id: Id): Int = {
    model.deleteModelById(id)
  }

  /**
   * Set notice flash message for successful completion of deletion.
   */
  protected def setDestroyCompletionFlash() = {
    flash += ("notice" -> createI18n().get(s"${resourceName}.flash.deleted").getOrElse(s"The ${resourceName} was deleted."))
  }

  /**
   * Destroys the specified resource.
   *
   * DELETE /{resources}/{id}
   *
   * @param id id
   * @param format format
   * @return result
   */
  def destroyResource(id: Id)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    model.findModel(id).map { m =>
      doDestroy(id)
      setDestroyCompletionFlash()
      status = 200
    } getOrElse haltWithBody(404)
  }

}

/**
 * Routings for Skinny resource.
 */
trait SkinnyResourceRoutes[Id] extends SkinnyController with Routes { self: SkinnyResourceActions[Id] =>

  /**
   * to enable params.getAs[Id]("id")
   */
  implicit val scalatraParamsIdTypeConverter: TypeConverter[String, Id] = new TypeConverter[String, Id] {
    def apply(s: String): Option[Id] = Option(s).map(_.asInstanceOf[Id])
  }

  // ------------------
  // Routing
  // ------------------

  /**
   * Pass this controller instance implicitly
   * because [[skinny.routing.implicits.RoutesAsImplicits]] expects [[skinny.controller.SkinnyControllerBase]].
   */
  private[this] implicit val skinnyController: SkinnyController = this

  // --------------
  // create

  // should be defined in front of 'show
  val newUrl = get(s"${resourcesBasePath}/new")(newResource).as('new)

  val createUrl = post(s"${resourcesBasePath}")(createResource).as('create)
  val createWithSlashUrl = post(s"${resourcesBasePath}/")(createResource).as('createWithSlash)

  // --------------
  // show

  val indexUrl = get(s"${resourcesBasePath}")(showResources()).as('index)
  val indexWithSlashUrl = get(s"${resourcesBasePath}/")(showResources()).as('indexWithSlash)

  val indexExtUrl = get(s"${resourcesBasePath}.:ext") {
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

  val showUrl = get(s"${resourcesBasePath}/:${idParamName}") {
    if (params.getAs[String](idParamName).exists(_ == "new")) {
      newResource()
    } else {
      params.getAs[Id](idParamName).map { id => showResource(id) } getOrElse haltWithBody(404)
    }
  }.as('show)

  val showExtUrl = get(s"${resourcesBasePath}/:${idParamName}.:ext") {
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

  val editUrl = get(s"${resourcesBasePath}/:${idParamName}/edit") {
    params.getAs[Id](idParamName).map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as('edit)

  val updatePostUrl = post(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  val updateUrl = put(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  val updatePatchUrl = patch(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  // --------------
  // delete

  val deleteUrl = delete(s"${resourcesBasePath}/:${idParamName}") {
    params.getAs[Id](idParamName).map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }.as('destroy)

}
