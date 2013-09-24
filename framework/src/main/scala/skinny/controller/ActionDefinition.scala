package skinny.controller

import org.scalatra.HttpMethod

case class ActionDefinition(name: Symbol, method: HttpMethod, matcher: (HttpMethod, String) => Boolean)
