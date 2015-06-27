package org.scalatra

import scala.language.postfixOps

import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest }
import scala.concurrent.Future
import scala.concurrent.duration._

abstract class AsyncResult(
  implicit override val scalatraContext: ScalatraContext)
    extends ScalatraContext {

  implicit val request: HttpServletRequest = scalatraContext.request

  implicit val response: HttpServletResponse = scalatraContext.response

  val servletContext: ServletContext = scalatraContext.servletContext

  // This is a Duration instead of a timeout because a duration has the concept of infinity
  implicit def timeout: Duration = 30 seconds

  val is: Future[_]

}
