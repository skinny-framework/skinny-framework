package skinny.controller

import skinny.micro.constant.HttpMethod

/**
  * Action definition.
  *
  * Action represents a tuple of the name, HTTP method and path matcher.
  * For example, actions will be used for predicating filters should be applied.
  *
  * @param name name for this action method
  * @param method http method
  * @param matcher path matcher
  */
case class ActionDefinition(
    name: String,
    method: HttpMethod,
    matcher: (HttpMethod, String) => Boolean
)
