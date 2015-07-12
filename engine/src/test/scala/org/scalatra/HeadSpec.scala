package org.scalatra

import org.scalatra.test.specs2.ScalatraSpec
import skinny.engine.SkinnyEngineServlet

class HeadSpec extends ScalatraSpec {
  def is =
    s2"""
A HEAD request should"
  return no body $noBody
  preserve headers $preserveHeaders
"""
  val servletHolder = addServlet(classOf[HeadSpecServlet], "/*")

  def noBody = head("/") { response.body must_== "" }

  def preserveHeaders = head("/") {
    header("X-Powered-By") must_== "caffeine"
  }
}

class HeadSpecServlet extends SkinnyEngineServlet {
  get("/") {
    response.addHeader("X-Powered-By", "caffeine")
    "poof -- watch me disappear"
  }
}
