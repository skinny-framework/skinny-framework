package skinny.controller

import skinny.micro.constant.HttpMethod

/**
  * Action definition.
  *
  * Action represents a tuple of symbol name, HTTP method and path matcher.
  * For example, actions will be used for predicating filters should be applied.
  *
  * @param name symbol value for this action method
  * @param method http method
  * @param matcher path matcher
  */
case class ActionDefinition(
    name: Symbol,
    method: HttpMethod,
    matcher: (HttpMethod, String) => Boolean
)
