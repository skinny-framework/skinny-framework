package skinny.controller.feature

import java.lang.reflect.Modifier
import skinny.controller.{ KeyAndErrorMessages, Params }
import skinny.engine.SkinnyEngineBase
import skinny.exception.RequestScopeConflictException
import java.util.Locale
import org.joda.time._
import skinny.I18n
import skinny.logging.LoggerProvider
import skinny.util.DateTimeUtil
import javax.servlet.http.HttpServletRequest

object RequestScopeFeature extends LoggerProvider {

  /**
   * Key for request scope.
   */
  val REQUEST_SCOPE_KEY = "__SKINNY_FRAMEWORK_REQUEST_SCOPE__"

  // skinny has this request scope as a member
  val ATTR_SKINNY = "s"

  val ATTR_CONTEXT_PATH = "contextPath"
  val ATTR_REQUEST_PATH = "requestPath"
  val ATTR_REQUEST_PATH_WITH_QUERY_STRING = "requestPathWithQueryString"
  val ATTR_PARAMS = "params"
  val ATTR_MULTI_PARAMS = "multiParams"
  val ATTR_FLASH = "flash"
  val ATTR_ERROR_MESSAGES = "errorMessages"
  val ATTR_KEY_AND_ERROR_MESSAGES = "keyAndErrorMessages"
  val ATTR_I18N = "i18n"

  val ATTR_CSRF_KEY = "csrfKey"
  val ATTR_CSRF_TOKEN = "csrfToken"

  // Used in the SkinnyResource & TemplateEngineFeature
  val ATTR_RESOURCE_NAME = "resourceName"
  val ATTR_RESOURCES_NAME = "resourcesName"

  /**
   * Returns request scope Map value.
   */
  def requestScope(request: HttpServletRequest): scala.collection.concurrent.Map[String, Any] = {
    request.getAttribute(REQUEST_SCOPE_KEY) match {
      case null =>
        val values = scala.collection.concurrent.TrieMap[String, Any]()
        request.setAttribute(REQUEST_SCOPE_KEY, values)
        values
      case values: scala.collection.concurrent.Map[_, _] =>
        values.asInstanceOf[scala.collection.concurrent.Map[String, Any]]
      case _ => throw new RequestScopeConflictException(
        s"Don't use '${REQUEST_SCOPE_KEY}' for request attribute key name.")
    }
  }

  /**
   * Fetches value from request scope.
   */
  def getAs[A](request: HttpServletRequest, key: String): Option[A] = {
    requestScope(request).get(key).map { v =>
      try v.asInstanceOf[A]
      catch {
        case e: ClassCastException =>
          throw new RequestScopeConflictException(
            s"""\"${key}\" value in request scope is unexpected. (actual: ${v}, error: ${e.getMessage}})""")
      }
    }
  }

}

/**
 * Request scope support.
 */
trait RequestScopeFeature extends SkinnyEngineBase with SnakeCasedParamKeysFeature with LocaleFeature with LoggerProvider {

  // ---------------------------------------------------
  // Notice: Due to org.scalatra.DynamicScope's implicit conversion, we need to specify request explicitly.

  // [error] ambiguous implicit values:
  // [error]  both value req of type javax.servlet.http.HttpServletRequest
  // [error]  and method request in class DashboardController of type => javax.servlet.http.HttpServletRequest
  // [error]  match expected type javax.servlet.http.HttpServletRequest
  // ---------------------------------------------------

  import RequestScopeFeature._

  /**
   * Registers default attributes in the request scope.
   */
  before()(initializeRequestScopeAttributes(request))

  def initializeRequestScopeAttributes(implicit req: HttpServletRequest) = {
    if (requestScope(req).get(ATTR_SKINNY).isEmpty) {
      set(ATTR_SKINNY, skinny.Skinny(requestScope(req)))(req)
      // requestPath/contextPath
      val requestPathWithContext = contextPath + requestPath(req)
      val queryStringPart = Option(request.getQueryString).map(qs => "?" + qs).getOrElse("")
      set(ATTR_CONTEXT_PATH -> contextPath)(req)
      set(ATTR_REQUEST_PATH -> requestPathWithContext)(req)
      set(ATTR_REQUEST_PATH_WITH_QUERY_STRING -> s"${requestPathWithContext}${queryStringPart}")(req)
      // for forms/validator
      set(ATTR_PARAMS -> skinny.controller.Params(params(req)))(req)
      set(ATTR_MULTI_PARAMS -> skinny.controller.MultiParams(multiParams(req)))(req)
      set(ATTR_ERROR_MESSAGES -> Seq())(req)
      set(ATTR_KEY_AND_ERROR_MESSAGES -> KeyAndErrorMessages())(req)
      // i18n in view templates
      setI18n(req)
    }
  }

  /**
   * Returns whole request scope attributes.
   *
   * @return whole attributes
   */
  def requestScope(implicit req: HttpServletRequest = request): scala.collection.concurrent.Map[String, Any] = {
    RequestScopeFeature.requestScope(req)
  }
  /**
   * Set attribute to request scope.
   *
   * @param keyAndValue key and value
   * @return self
   */
  def requestScope(keyAndValue: (String, Any))(implicit req: HttpServletRequest): RequestScopeFeature = {
    requestScope(Seq(keyAndValue))(req)
  }

  /**
   * Set attributes to request scope.
   *
   * @param keyAndValues collection of key and value.
   * @return self
   */
  def requestScope(keyAndValues: Seq[(String, Any)])(implicit req: HttpServletRequest): RequestScopeFeature = {
    keyAndValues.foreach {
      case (key, _) =>
        if (key == "layout") {
          logger.warn("'layout' is a special attribute for Scalate. " +
            "If you're not going to replace layout template, use another key for this attribute. " +
            "Or if you'd like to change layout for this action, use layout(\"/other\") instead.")
        }
    }
    requestScope(req) ++= keyAndValues
    this
  }

  /**
   * Set attribute to request scope.
   */
  def set(keyAndValue: (String, Any))(implicit req: HttpServletRequest): RequestScopeFeature = {
    requestScope(Seq(keyAndValue))(req)
  }

  /**
   * Set attributes to request scope.
   */
  def set(keyAndValues: Seq[(String, Any)])(implicit req: HttpServletRequest): RequestScopeFeature = {
    requestScope(keyAndValues)(req)
  }

  @deprecated("Use #getFromRequestScope instead. This method will be removed since 1.3.", "1.2.0")
  def requestScope[A](key: String): Option[A] = getFromRequestScope(key)(request)

  /**
   * Fetches value from request scope.
   *
   * @param key key
   * @tparam A type
   * @return value if exists
   */
  def getFromRequestScope[A](key: String)(implicit req: HttpServletRequest = request): Option[A] = {
    requestScope(req).get(key).map { v =>
      try v.asInstanceOf[A]
      catch {
        case e: ClassCastException =>
          val message = s"""\"${key}\" value in request scope is unexpected. (actual: ${v}, error: ${e.getMessage}})"""
          throw new RequestScopeConflictException(message)
      }
    }
  }

  /**
   * Set params which is generated from a model object using Java reflection APIs.
   *
   * @param model model instance
   */
  def setAsParams(model: Any)(implicit req: HttpServletRequest = request): Unit = {
    val toKey: (String) => String = if (useSnakeCasedParamKeys) {
      skinny.util.StringUtil.toSnakeCase
    } else {
      (value: String) => value
    }

    getterNamesFromEntity(model).foreach { getterName =>
      val value = model.getClass.getDeclaredMethod(getterName).invoke(model)
      addParam(toKey(getterName), value)(req)

      val rawValue = value match {
        case Some(raw) => raw
        case None => null
        case _ => value
      }
      rawValue match {
        case dt: DateTime =>
          addParam(toKey(s"${getterName}Year"), dt.getYearOfEra)(req)
          addParam(toKey(s"${getterName}Month"), dt.getMonthOfYear)(req)
          addParam(toKey(s"${getterName}Day"), dt.getDayOfMonth)(req)
          addParam(toKey(s"${getterName}Hour"), dt.getHourOfDay)(req)
          addParam(toKey(s"${getterName}Minute"), dt.getMinuteOfHour)(req)
          addParam(toKey(s"${getterName}Second"), dt.getSecondOfMinute)(req)
          addParam(toKey(s"${getterName}Date"), DateTimeUtil.toString(dt.toLocalDate))(req)
          addParam(toKey(s"${getterName}Time"), DateTimeUtil.toString(dt.toLocalTime))(req)

        case ld: LocalDate =>
          addParam(toKey(s"${getterName}Year"), ld.getYearOfEra)(req)
          addParam(toKey(s"${getterName}Month"), ld.getMonthOfYear)(req)
          addParam(toKey(s"${getterName}Day"), ld.getDayOfMonth)(req)

        case lt: LocalTime =>
          addParam(toKey(s"${getterName}Hour"), lt.getHourOfDay)(req)
          addParam(toKey(s"${getterName}Minute"), lt.getMinuteOfHour)(req)
          addParam(toKey(s"${getterName}Second"), lt.getSecondOfMinute)(req)

        case _ =>
      }
    }
  }

  /**
   * Returns getter names from the target object.
   *
   * @param obj object
   * @return getter names
   */
  private def getterNamesFromEntity(obj: Any): Seq[String] = {
    val fieldNames = obj.getClass.getDeclaredFields
      .filter(f => Modifier.isPrivate(f.getModifiers))
      .filterNot(f => Modifier.isStatic(f.getModifiers))
      .map(_.getName)

    val methodNames = obj.getClass.getDeclaredMethods
      .filter(m => Modifier.isPublic(m.getModifiers))
      .filterNot(m => Modifier.isStatic(m.getModifiers))
      .filterNot(m => m.getParameterTypes.size > 0)
      .map(_.getName)

    methodNames.filter(m => fieldNames.contains(m))
  }

  /**
   * Set params to request scope.
   */
  def setParams(implicit req: HttpServletRequest = request) = setParamsToRequestScope(req)

  /**
   * Set params to request scope.
   */
  def setParamsToRequestScope(implicit req: HttpServletRequest = request): Unit = set(ATTR_PARAMS -> Params(params(req)))(req)

  /**
   * Set {{skinny.I18n}} object for the current request to request scope.
   *
   * @param locale current locale
   * @return self
   */
  def setI18n(implicit req: HttpServletRequest = request, locale: Locale = currentLocale(request).orNull[Locale]) = {
    set(RequestScopeFeature.ATTR_I18N, I18n(locale))(req)
  }

  /**
   * Add param to params in the request scope.
   *
   * @param name name
   * @param value value
   */
  def addParam(name: String, value: Any)(implicit req: HttpServletRequest = request): Unit = {

    // ensure "params" in the request scope is valid
    // don't delete requestScope[Any] 's `Any` (because cannot cast Nothing to Params)
    val isParamsInRequestScopeValid = getFromRequestScope[Any](ATTR_PARAMS)(req).map(_.isInstanceOf[Params]).getOrElse(false)

    if (isParamsInRequestScopeValid) {
      val updatedParams: Params = {
        Params(getFromRequestScope[Params](ATTR_PARAMS)(req)
          .map(_.underlying)
          .getOrElse(params(req))
          .updated(name, value))
      }
      set(RequestScopeFeature.ATTR_PARAMS -> updatedParams)(req)

    } else {
      val actual = getFromRequestScope(ATTR_PARAMS)(req)
      throw new RequestScopeConflictException(
        s"""Skinny Framework expects that $${params} is a SkinnyParams value. (actual: "${actual}", class: ${actual.getClass.getName})""")
    }
  }

  /**
   * Returns errorMessages in the RequestScope.
   */
  def errorMessages(implicit req: HttpServletRequest = request): Seq[String] = {
    requestScope(req).get(RequestScopeFeature.ATTR_ERROR_MESSAGES)
      .map(_.asInstanceOf[Seq[String]]).getOrElse(Nil)
  }

  /**
   * Returns keyAndErrorMessages in the RequestScope.
   */
  def keyAndErrorMessages(implicit req: HttpServletRequest = request): Map[String, Seq[String]] = {
    requestScope(req).get(RequestScopeFeature.ATTR_KEY_AND_ERROR_MESSAGES)
      .map(_.asInstanceOf[KeyAndErrorMessages].underlying).getOrElse(Map())
  }

}
