package skinny.controller.feature

import java.lang.reflect.Modifier
import org.scalatra.ScalatraBase
import skinny.controller.{ KeyAndErrorMessages, SkinnyControllerBase, Params }
import skinny.exception.RequestScopeConflictException
import java.util.Locale
import org.joda.time._
import skinny.I18n
import grizzled.slf4j.Logging

object RequestScopeFeature {

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

  val ATTR_RESOURCE_NAME = "resourceName"
  val ATTR_RESOURCES_NAME = "resourcesName"
}

/**
 * Request scope support.
 */
trait RequestScopeFeature extends ScalatraBase with SnakeCasedParamKeysFeature with SessionLocaleFeature with Logging {

  import RequestScopeFeature._

  /**
   * Registers default attributes in the request scope.
   */
  before() {
    if (requestScope().isEmpty) {
      set(ATTR_SKINNY, skinny.Skinny(requestScope()))
      // requestPath/contextPath
      val requestPathWithContext = contextPath + requestPath
      val queryStringPart = Option(request.getQueryString).map(qs => "?" + qs).getOrElse("")
      set(ATTR_CONTEXT_PATH -> contextPath)
      set(ATTR_REQUEST_PATH -> requestPathWithContext)
      set(ATTR_REQUEST_PATH_WITH_QUERY_STRING -> s"${requestPathWithContext}${queryStringPart}")
      // for forms/validator
      set(ATTR_PARAMS -> skinny.controller.Params(params))
      set(ATTR_MULTI_PARAMS -> skinny.controller.MultiParams(multiParams))
      set(ATTR_ERROR_MESSAGES -> Seq())
      set(ATTR_KEY_AND_ERROR_MESSAGES -> KeyAndErrorMessages())
      // i18n in view templates
      setI18n()
    }
  }

  /**
   * Returns whole request scope attributes.
   *
   * @return whole attributes
   */
  def requestScope(): scala.collection.concurrent.Map[String, Any] = {
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
   * Set attribute to request scope.
   *
   * @param keyAndValue key and value
   * @return self
   */
  def requestScope(keyAndValue: (String, Any)): RequestScopeFeature = requestScope(Seq(keyAndValue))

  /**
   * Set attributes to request scope.
   *
   * @param keyAndValues collection of key and value.
   * @return self
   */
  def requestScope(keyAndValues: Seq[(String, Any)]): RequestScopeFeature = {
    keyAndValues.foreach {
      case (key, _) =>
        if (key == "layout") {
          logger.warn("'layout' is a special attribute for Scalate. " +
            "If you're not going to replace layout template, use another key for this attribute. " +
            "Or if you'd like to change layout for this action, use layout(\"/other\") instead.")
        }
    }
    requestScope ++= keyAndValues
    this
  }

  /**
   * Set attribute to request scope.
   */
  def set(keyAndValue: (String, Any)): RequestScopeFeature = requestScope(Seq(keyAndValue))

  /**
   * Set attributes to request scope.
   */
  def set(keyAndValues: Seq[(String, Any)]): RequestScopeFeature = requestScope(keyAndValues)

  /**
   * Fetches value from request scope.
   *
   * @param key key
   * @tparam A type
   * @return value if exists
   */
  def requestScope[A](key: String): Option[A] = {
    requestScope.get(key).map { v =>
      try v.asInstanceOf[A]
      catch {
        case e: ClassCastException =>
          throw new RequestScopeConflictException(
            s"""\"${key}\" value in request scope is unexpected. (actual: ${v}, error: ${e.getMessage}})""")
      }
    }
  }

  /**
   * Set params which is generated from a model object using Java reflection APIs.
   *
   * @param model model instance
   */
  def setAsParams(model: Any): Unit = {
    import skinny.util.StringUtil.toSnakeCase

    getterNamesFromEntity(model).foreach { getterName =>
      val value = model.getClass.getDeclaredMethod(getterName).invoke(model)
      if (useSnakeCaseKeys) {
        addParam(toSnakeCase(getterName), value)
      } else {
        addParam(getterName, value)
      }

      value match {
        case opt: Option[_] => opt foreach {
          case dt: DateTime =>
            if (useSnakeCaseKeys) {
              addParam(toSnakeCase(s"${getterName}Year"), dt.getYearOfEra)
              addParam(toSnakeCase(s"${getterName}Month"), dt.getMonthOfYear)
              addParam(toSnakeCase(s"${getterName}Day"), dt.getDayOfMonth)
              addParam(toSnakeCase(s"${getterName}Hour"), dt.getHourOfDay)
              addParam(toSnakeCase(s"${getterName}Minute"), dt.getMinuteOfHour)
              addParam(toSnakeCase(s"${getterName}Second"), dt.getSecondOfMinute)
            } else {
              addParam(s"${getterName}Year", dt.getYearOfEra)
              addParam(s"${getterName}Month", dt.getMonthOfYear)
              addParam(s"${getterName}Day", dt.getDayOfMonth)
              addParam(s"${getterName}Hour", dt.getHourOfDay)
              addParam(s"${getterName}Minute", dt.getMinuteOfHour)
              addParam(s"${getterName}Second", dt.getSecondOfMinute)
            }

          case ld: LocalDate =>
            if (useSnakeCaseKeys) {
              addParam(toSnakeCase(s"${getterName}Year"), ld.getYearOfEra)
              addParam(toSnakeCase(s"${getterName}Month"), ld.getMonthOfYear)
              addParam(toSnakeCase(s"${getterName}Day"), ld.getDayOfMonth)
            } else {
              addParam(s"${getterName}Year", ld.getYearOfEra)
              addParam(s"${getterName}Month", ld.getMonthOfYear)
              addParam(s"${getterName}Day", ld.getDayOfMonth)
            }

          case lt: LocalTime =>
            if (useSnakeCaseKeys) {
              addParam(toSnakeCase(s"${getterName}Hour"), lt.getHourOfDay)
              addParam(toSnakeCase(s"${getterName}Minute"), lt.getMinuteOfHour)
              addParam(toSnakeCase(s"${getterName}Second"), lt.getSecondOfMinute)
            } else {
              addParam(s"${getterName}Hour", lt.getHourOfDay)
              addParam(s"${getterName}Minute", lt.getMinuteOfHour)
              addParam(s"${getterName}Second", lt.getSecondOfMinute)
            }

          case value =>
        }
        case dt: DateTime =>
          if (useSnakeCaseKeys) {
            addParam(toSnakeCase(s"${getterName}Year"), dt.getYearOfEra)
            addParam(toSnakeCase(s"${getterName}Month"), dt.getMonthOfYear)
            addParam(toSnakeCase(s"${getterName}Day"), dt.getDayOfMonth)
            addParam(toSnakeCase(s"${getterName}Hour"), dt.getHourOfDay)
            addParam(toSnakeCase(s"${getterName}Minute"), dt.getMinuteOfHour)
            addParam(toSnakeCase(s"${getterName}Second"), dt.getSecondOfMinute)
          } else {
            addParam(s"${getterName}Year", dt.getYearOfEra)
            addParam(s"${getterName}Month", dt.getMonthOfYear)
            addParam(s"${getterName}Day", dt.getDayOfMonth)
            addParam(s"${getterName}Hour", dt.getHourOfDay)
            addParam(s"${getterName}Minute", dt.getMinuteOfHour)
            addParam(s"${getterName}Second", dt.getSecondOfMinute)
          }

        case ld: LocalDate =>
          if (useSnakeCaseKeys) {
            addParam(toSnakeCase(s"${getterName}Year"), ld.getYearOfEra)
            addParam(toSnakeCase(s"${getterName}Month"), ld.getMonthOfYear)
            addParam(toSnakeCase(s"${getterName}Day"), ld.getDayOfMonth)
          } else {
            addParam(s"${getterName}Year", ld.getYearOfEra)
            addParam(s"${getterName}Month", ld.getMonthOfYear)
            addParam(s"${getterName}Day", ld.getDayOfMonth)
          }

        case lt: LocalTime =>
          if (useSnakeCaseKeys) {
            addParam(toSnakeCase(s"${getterName}Hour"), lt.getHourOfDay)
            addParam(toSnakeCase(s"${getterName}Minute"), lt.getMinuteOfHour)
            addParam(toSnakeCase(s"${getterName}Second"), lt.getSecondOfMinute)
          } else {
            addParam(s"${getterName}Hour", lt.getHourOfDay)
            addParam(s"${getterName}Minute", lt.getMinuteOfHour)
            addParam(s"${getterName}Second", lt.getSecondOfMinute)
          }

        case value =>
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
  def setParams() = setParamsToRequestScope()

  /**
   * Set params to request scope.
   */
  def setParamsToRequestScope(): Unit = set(ATTR_PARAMS -> Params(params))

  /**
   * Set {{skinny.I18n}} object for the current request to request scope.
   *
   * @param locale current locale
   * @return self
   */
  def setI18n()(implicit locale: Locale = currentLocale.orNull[Locale]) = {
    set(RequestScopeFeature.ATTR_I18N, I18n(locale))
  }

  /**
   * Add param to params in the request scope.
   *
   * @param name name
   * @param value value
   */
  def addParam(name: String, value: Any): Unit = {

    // ensure "params" in the request scope is valid
    // don't delete requestScope[Any] 's `Any` (because cannot cast Nothing to Params)
    val isParamsInRequestScopeValid = requestScope[Any](ATTR_PARAMS).map(_.isInstanceOf[Params]).getOrElse(false)

    if (isParamsInRequestScopeValid) {
      val updatedParams: Params = {
        Params(requestScope[Params]("params")
          .map(_.underlying)
          .getOrElse(params)
          .updated(name, value))
      }
      set(RequestScopeFeature.ATTR_PARAMS -> updatedParams)

    } else {
      val actual = requestScope(ATTR_PARAMS)
      throw new RequestScopeConflictException(
        s"""Skinny Framework expects that $${params} is a SkinnyParams value. (actual: "${actual}", class: ${actual.getClass.getName})""")
    }
  }

}
