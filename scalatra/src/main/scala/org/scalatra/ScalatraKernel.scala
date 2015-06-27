package org.scalatra

object ScalatraKernel {

  @deprecated("Use org.scalatra.MultiParams", "1.4")
  type MultiParams = util.MultiMap

  @deprecated("Use org.scalatra.Action", "1.4")
  type Action = () => Any

  @deprecated("Use HttpMethod.methods", "1.4")
  val httpMethods = HttpMethod.methods map { _.toString }

  @deprecated("Use HttpMethod.methods filter { !_.isSafe }", "1.4")
  val writeMethods = HttpMethod.methods filter { !_.isSafe } map { _.toString }

  @deprecated("Use CsrfTokenSupport.DefaultKey", "1.4")
  val csrfKey = CsrfTokenSupport.DefaultKey

  @deprecated("Use org.scalatra.EnvironmentKey", "1.4")
  val EnvironmentKey = "org.scalatra.environment"

  @deprecated("Use org.scalatra.MultiParamsKey", "1.4")
  val MultiParamsKey = "org.scalatra.MultiParams"

}
