package skinny.controller

import skinny.micro.SkinnyMicroServlet

/**
  * Skinny Servlet Controller for REST APIs.
  *
  * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
  */
trait SkinnyApiServlet extends SkinnyMicroServlet with SkinnyControllerBase
