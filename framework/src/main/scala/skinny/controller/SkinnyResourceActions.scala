package skinny.controller

import skinny._
import skinny.ParamType
import skinny.validator.{ NewValidation, MapValidator }
import skinny.exception.StrongParametersException
import java.util.Locale
import org.scalatra.util.conversion.{ Conversions, TypeConverter }
import skinny.controller.feature.RequestScopeFeature

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
      val pageNo: Int = params.getAs[Int](pageNoParamName).getOrElse(1)
      val totalCount: Long = countResources()
      val totalPages: Int = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)

      set(itemsName, findResources(pageSize, pageNo))
      set(totalPagesAttributeName -> totalPages)
    } else {
      set(itemsName, findResources())
    }
    render(s"${viewsDirectoryPath}/index")
  }

  protected def countResources(): Long = model.countAllModels()

  protected def findResources(pageSize: Int, pageNo: Int): List[_] = model.findModels(pageSize, pageNo)

  protected def findResources(): List[_] = model.findAllModels()

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
    set(itemName, findResource(id).getOrElse(haltWithBody(404)))
    render(s"${viewsDirectoryPath}/show")
  }

  protected def findResource(id: Id): Option[_] = model.findModel(id)

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
      format match {
        case Format.HTML => render(s"${viewsDirectoryPath}/new")
        case _ => renderWithFormat(keyAndErrorMessages)
      }
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
        format match {
          case Format.HTML => render(s"${viewsDirectoryPath}/edit")
          case _ => renderWithFormat(keyAndErrorMessages)
        }
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
