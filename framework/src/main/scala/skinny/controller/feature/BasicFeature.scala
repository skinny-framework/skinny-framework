package skinny.controller.feature

import skinny.controller.{ Constants, ActionDefinition }
import org.scalatra._

trait BasicFeature extends ScalatraBase { self: RequestScopeFeature =>

  before() {
    set("contextPath", contextPath)
    set("requestPath", contextPath + requestPath)
    set("requestPathWithQueryString", s"${contextPath}${requestPath}${Option(request.getQueryString).map(qs => "?" + qs).getOrElse("")}")
    set("params", skinny.controller.Params(params))
    set("errorMessages" -> Seq())
    set("keyAndErrorMessages" -> Map[String, Seq[String]]())
    setI18n()
  }

  override protected def addRoute(method: HttpMethod, transformers: Seq[RouteTransformer], action: => Any): Route = {
    val route = super.addRoute(method, transformers, action)
    route.copy(metadata = route.metadata.updated(Constants.RouteMetadataHttpMethodCacheKey, method))
  }

  // Same action name should be registered several times
  // for example, put("/members/:id")(...).as('update) and patch("/members/:id")(...).as('update)
  val actionDefinitions = new scala.collection.mutable.ArrayBuffer[ActionDefinition]

  def addActionDefinition(actionDef: ActionDefinition) = {
    actionDefinitions += actionDef
  }

  def currentActionName: Option[Symbol] = {
    actionDefinitions.find { actionDef =>
      actionDef.matcher.apply(HttpMethod(request.getMethod), requestPath)
    }.map(_.name)
  }

}
