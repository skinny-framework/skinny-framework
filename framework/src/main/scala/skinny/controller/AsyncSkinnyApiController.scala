package skinny.controller

import skinny.micro.AsyncSkinnyMicroFilter

/**
  * SkinnyController as a Servlet for REST APIs.
  *
  * NOTICE: If you'd like to disable Set-Cookie header for session id, configure in web.xml
  */
trait AsyncSkinnyApiController extends AsyncSkinnyControllerBase with AsyncSkinnyMicroFilter
