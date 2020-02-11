package skinny.controller.feature

import skinny.controller.ActionDefinition
import skinny.micro.SkinnyMicroBase
import skinny.micro.constant.HttpMethod
import skinny.micro.context.SkinnyContext

/**
  * Action definitions for this controller.
  * These definitions will be used for beforeAction/afterAction's only/except.
  */
trait ActionDefinitionFeature extends SkinnyMicroBase {

  /**
    * Note: Same action method name should be registered several times.
    * For example, put("/members/:id")(...).as('update) and patch("/members/:id")(...).as('update).
    */
  protected val actionDefinitions = new scala.collection.mutable.ArrayBuffer[ActionDefinition]

  /**
    * Adds action definition
    *
    * @param actionDef action definition
    */
  def addActionDefinition(actionDef: ActionDefinition): Unit = {
    actionDefinitions += actionDef
  }

  /**
    * Returns action name for this request.
    *
    * @return action name
    */
  def currentActionName(implicit cxt: SkinnyContext = context): Option[String] = {
    // Scalatra takes priority to routing definition which is defined later.
    // So we should take priority to the first action name found from reversed action definitions here.
    actionDefinitions.reverse
      .find { actionDef =>
        actionDef.matcher.apply(HttpMethod(request.getMethod), requestPath(cxt))
      }
      .map(_.name)
  }

}
