package skinny.controller

import skinny._
import skinny.ParamType
import skinny.validator.{ NewValidation, MapValidator }
import skinny.exception.StrongParametersException
import java.util.Locale

/**
 * Skinny resource is a DRY module to implement ROA(Resource-oriented architecture) apps.
 *
 * SkinnyResource is surely inspired by Rails ActiveSupport.
 */
trait SkinnyResource extends SkinnyController {

  /**
   * SkinnyModel for this resource.
   */
  protected def model: SkinnyModel[_]

  /**
   * Resource name in the plural. This name will be used for path and directory name to locale template files.
   */
  protected def resourcesName: String

  /**
   * Resource name in the singular. This name will be used for path and validator's prefix.
   */
  protected def resourceName: String

  override protected def xmlItemName = resourceName

  /**
   * Creates validator with prefix(resourceName).
   *
   * @param validations validations validations
   * @param locale current locale
   * @return validator
   */
  override def validation(validations: NewValidation*)(implicit locale: Locale = currentLocale.orNull[Locale]): MapValidator = {
    validationWithPrefix(resourceName, validations: _*)
  }

  /**
   * Use relative path if true. This is set as false by default.
   *
   * If you set this as true, routing will become simpler but /${resources}.xml or /${resources}.json don't work.
   */
  protected def useRelativePath: Boolean = false

  /**
   * Base path.
   */
  protected def basePath: String = if (useRelativePath) "" else s"/${resourcesName}"

  /**
   * Outputs debug logging for passed parameters.
   *
   * @param form input form
   * @param id id if exists
   */
  protected def debugLoggingParameters(form: MapValidator, id: Option[Long] = None) = {
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
  protected def debugLoggingPermittedParameters(parameters: PermittedStrongParameters, id: Option[Long] = None) = {
    val forId = id.map { id => s" for [id -> ${id}]" }.getOrElse("")
    val params = parameters.params.map { case (name, (v, t)) => s"${name} -> '${v}' as ${t}" }.mkString("[", ", ", "]")
    logger.debug(s"Permitted parameters${forId}: ${params}")
  }

  // ----------------------------
  //  Actions for this resource
  // ----------------------------

  /**
   * Shows a list of resource.
   *
   * GET /{resources}/
   * GET /{resources}
   * GET /{resources}.xml
   * GET /{resources}.json
   *
   * @param format format
   * @return list of resource
   */
  def showResources()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    set(resourcesName, model.findAllModels())
    render(s"/${resourcesName}/index")
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
  def showResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    set(resourceName, model.findModel(id).getOrElse(haltWithBody(404)))
    render(s"/${resourcesName}/show")
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
    render(s"/${resourcesName}/new")
  }

  /**
   * Input form for creation
   */
  protected def createForm: MapValidator

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
  protected def doCreateAndReturnId(parameters: PermittedStrongParameters): Long = {
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
      val id: Long = if (!createFormStrongParameters.isEmpty) {
        val parameters = params.permit(createFormStrongParameters: _*)
        doCreateAndReturnId(parameters)
      } else {
        throw new StrongParametersException(
          "'createFormStrongParameters' or 'createFormTypedStrongParameters' must be defined.")
      }
      format match {
        case Format.HTML =>
          setCreateCompletionFlash()
          redirect(s"/${resourcesName}/${id}")
        case _ =>
          status = 201
          response.setHeader("Location", s"${contextPath}/${resourcesName}/${id}")
      }
    } else {
      render(s"/${resourcesName}/new")
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
  def editResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    model.findModel(id).map {
      m =>
        status = 200
        format match {
          case Format.HTML =>
            setAsParams(m)
            render(s"/${resourcesName}/edit")
          case _ =>
        }
    } getOrElse haltWithBody(404)
  }

  /**
   * Input form for modification
   */
  protected def updateForm: MapValidator

  /**
   * Strong parameter definitions for mofidication form
   */
  protected def updateFormStrongParameters: Seq[(String, ParamType)]

  /**
   * Executes modification for the specified resource.
   *
   * @param id  id
   * @param parameters permitted parameters
   * @return count
   */
  protected def doUpdate(id: Long, parameters: PermittedStrongParameters): Int = {
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
  def updateResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    debugLoggingParameters(updateForm, Some(id))

    model.findModel(id).map { m =>
      if (updateForm.validate()) {
        if (!updateFormStrongParameters.isEmpty) {
          val parameters = params.permit(updateFormStrongParameters: _*)
          doUpdate(id, parameters)
        } else {
          throw new StrongParametersException(
            "'updateFormStrongParameters' or 'updateFormTypedStrongParameters' must be defined.")
        }
        status = 200
        format match {
          case Format.HTML =>
            setUpdateCompletionFlash()
            set(resourceName, model.findModel(id).getOrElse(haltWithBody(404)))
            render(s"/${resourcesName}/show")
          case _ =>
        }
      } else {
        render(s"/${resourcesName}/edit")
      }
    } getOrElse haltWithBody(404)
  }

  /**
   * Executes deletion of the specified single resource.
   *
   * @param id id
   * @return count
   */
  protected def doDestroy(id: Long): Int = {
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
  def destroyResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    model.findModel(id).map { m =>
      doDestroy(id)
      setDestroyCompletionFlash()
      status = 200
    } getOrElse haltWithBody(404)
  }

  // ------------------
  // Routing
  // ------------------

  /**
   * Pass this controller instance implicitly
   * because [[skinny.routing.implicits.RoutesAsImplicits]] expects [[skinny.controller.SkinnyControllerBase]].
   */
  private[this] implicit val skinnyController: SkinnyController = this

  // set resoureceName/resourcesName to the request scope
  before() {
    set("resourceName", resourceName)
    set("resourcesName", resourcesName)
  }

  // --------------
  // create

  // should be defined in front of 'show
  get(s"${basePath}/new")(newResource).as('new)

  post(s"${basePath}/?")(createResource).as('create)

  // --------------
  // show

  get(s"${basePath}/?")(showResources()).as('index)

  get(s"${basePath}.:ext") {
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

  get(s"${basePath}/:id") {
    if (params.getAs[String]("id").exists(_ == "new")) {
      newResource()
    } else {
      params.getAs[Long]("id").map { id => showResource(id) } getOrElse haltWithBody(404)
    }
  }.as('show)

  get(s"${basePath}/:id.:ext") {
    (for {
      id <- params.getAs[Long]("id")
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

  get(s"${basePath}/:id/edit") {
    params.getAs[Long]("id").map(id => editResource(id)) getOrElse haltWithBody(404)
  }.as('edit)

  post(s"${basePath}/:id") {
    params.getAs[Long]("id").map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  put(s"${basePath}/:id") {
    params.getAs[Long]("id").map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  patch(s"${basePath}/:id") {
    params.getAs[Long]("id").map(id => updateResource(id)) getOrElse haltWithBody(404)
  }.as('update)

  // --------------
  // delete

  delete(s"${basePath}/:id") {
    params.getAs[Long]("id").map(id => destroyResource(id)) getOrElse haltWithBody(404)
  }.as('destroy)

}
