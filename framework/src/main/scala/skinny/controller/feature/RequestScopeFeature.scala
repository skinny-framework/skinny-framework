package skinny.controller.feature

import java.lang.reflect.Modifier
import org.scalatra.ScalatraBase
import skinny.controller.Params
import skinny.exception.RequestScopeConflictException
import java.util.Locale
import skinny.I18n

/**
 * Request scope support.
 */
trait RequestScopeFeature extends ScalatraBase with SessionLocaleFeature {

  /**
   * Registers default attributes in the request scope.
   */
  before() {
    if (requestScope().isEmpty) {
      // requestPath/contextPath
      val requestPathWithContext = contextPath + requestPath
      val queryStringPart = Option(request.getQueryString).map(qs => "?" + qs).getOrElse("")
      set("contextPath", contextPath)
      set("requestPath", requestPathWithContext)
      set("requestPathWithQueryString", s"${requestPathWithContext}${queryStringPart}")
      // for forms/validator
      set("params", skinny.controller.Params(params))
      set("errorMessages" -> Seq())
      set("keyAndErrorMessages" -> Map[String, Seq[String]]())
      // i18n in view templates
      setI18n()
    }
  }

  /**
   * Key for request scope.
   */
  private[this] val SKINNY_REQUEST_SCOPE_KEY = "__SKINNY_FRAMEWORK_REQUEST_SCOPE__"

  /**
   * Returns whole request scope attributes.
   *
   * @return whole attributes
   */
  def requestScope(): scala.collection.mutable.Map[String, Any] = {
    request.getAttribute(SKINNY_REQUEST_SCOPE_KEY) match {
      case null =>
        val values = collection.mutable.Map[String, Any]()
        request.setAttribute(SKINNY_REQUEST_SCOPE_KEY, values)
        values
      case values: collection.mutable.Map[_, _] => values.asInstanceOf[collection.mutable.Map[String, Any]]
      case _ => throw new RequestScopeConflictException(
        s"Don't use '${SKINNY_REQUEST_SCOPE_KEY}' for request attribute key name.")
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
      case (key, value) =>
        value match {
          case Some(v) => requestScope += (key -> v)
          case None =>
          case v => requestScope += (key -> v)
        }
    }
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
    getterNames(model).foreach { getterName =>
      val value = model.getClass.getDeclaredMethod(getterName).invoke(model)
      addParam(getterName, value)
    }
  }

  /**
   * Returns getter names from the target object.
   *
   * @param obj object
   * @return getter names
   */
  private[this] def getterNames(obj: Any): Seq[String] = {
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
  def setParamsToRequestScope(): Unit = set("params" -> Params(params))

  /**
   * Set {{skinny.I18n}} object for the current request to request scope.
   *
   * @param locale current locale
   * @return self
   */
  def setI18n()(implicit locale: Locale = currentLocale.orNull[Locale]) = {
    set("i18n", I18n(locale))
  }

  /**
   * Add param to params in the request scope.
   *
   * @param name name
   * @param value value
   */
  def addParam(name: String, value: Any): Unit = {

    // ensure "params" in the request scope is valid
    val isParamsInRequestScopeValid = requestScope[Any]("params").map(_.isInstanceOf[Params]).getOrElse(false)

    if (isParamsInRequestScopeValid) {
      val updatedParams: Params = {
        Params(requestScope[Params]("params")
          .map(_.underlying)
          .getOrElse(params)
          .updated(name, value))
      }
      set("params" -> updatedParams)

    } else {
      val actual = requestScope("params")
      throw new RequestScopeConflictException(
        s"""Skinny Framework expects that $${params} is a SkinnyParams value. (actual: "${actual}", class: ${actual.getClass.getName})""")
    }
  }

}
