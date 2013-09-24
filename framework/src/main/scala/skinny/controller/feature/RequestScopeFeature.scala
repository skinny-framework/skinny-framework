package skinny.controller.feature

import java.lang.reflect.Modifier
import org.scalatra.ScalatraBase
import skinny.controller.Params
import skinny.exception.RequestScopeConflictException
import java.util.Locale
import skinny.I18n

trait RequestScopeFeature extends ScalatraBase {

  private[this] val SKINNY_REQUEST_SCOPE_KEY = "SCALATRA_SKINNY_REQUEST_SCOPE"

  def requestScope(): scala.collection.mutable.Map[String, Any] = {
    val values = request.getAttribute(SKINNY_REQUEST_SCOPE_KEY).asInstanceOf[scala.collection.mutable.Map[String, Any]]
    if (values != null) {
      values
    } else {
      val values = scala.collection.mutable.Map[String, Any]()
      request.setAttribute(SKINNY_REQUEST_SCOPE_KEY, values)
      values
    }
  }

  def requestScope(keyAndValue: (String, Any)): RequestScopeFeature = requestScope(Seq(keyAndValue))
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

  def requestScope[A](key: String): Option[A] = {
    requestScope.get(key).map { v =>
      try v.asInstanceOf[A]
      catch {
        case e: ClassCastException =>
          throw new IllegalStateException(s"""\"${key}\" value in request scope is unexpected. (actual: ${v}, error: ${e.getMessage}})""")
      }
    }
  }

  def set(keyAndValue: (String, Any)): RequestScopeFeature = requestScope(Seq(keyAndValue))
  def set(keyAndValues: Seq[(String, Any)]): RequestScopeFeature = requestScope(keyAndValues)

  def setAsParams(model: Any): Unit = {
    getterNames(model).foreach { getterName =>
      val value = model.getClass.getDeclaredMethod(getterName).invoke(model)
      addParam(getterName, value)
    }
  }

  private def getterNames(obj: Any): Seq[String] = {
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

  def setParams() = setParamsToRequestScope()
  def setParamsToRequestScope() {
    set("params" -> Params(params))
  }

  def setI18n()(implicit locale: Locale = null) {
    set("i18n", I18n(locale))
  }

  def addParam(name: String, value: Any): Unit = {
    val validTyped = requestScope[Any]("params").map(_.isInstanceOf[Params]).getOrElse(false)
    if (validTyped) {
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
