package skinny.controller

import skinny._
import skinny.ParamType
import skinny.validator.{ NewValidation, MapValidator }
import skinny.exception.StrongParametersException
import java.util.Locale

/**
 * Actions for Skinny API resource.
 */
trait SkinnyApiResourceActions[Id] { self: SkinnyControllerBase =>

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
      renderWithFormat(findResources(pageSize, pageNo))
    } else {
      renderWithFormat(findResources())
    }
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
    renderWithFormat(findResource(id).getOrElse(haltWithBody(404)))
  }

  protected def findResource(id: Id): Option[_] = model.findModel(id)

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
      status = 201
      val ext = if (format == Format.HTML) "" else "." + format.name
      response.setHeader("Location", s"${contextPath}/${normalizedResourcesBasePath}/${model.idToRawValue(id)}${ext}")
    } else {
      status = 400
      renderWithFormat(keyAndErrorMessages)
    }
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
      } else {
        status = 400
        renderWithFormat(keyAndErrorMessages)
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
      status = 200
    } getOrElse haltWithBody(404)
  }

}
