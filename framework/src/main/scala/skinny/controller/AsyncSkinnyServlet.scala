package skinny.controller

import skinny.micro.AsyncSkinnyMicroServlet

/**
  * SkinnyController as a Servlet.
  */
class AsyncSkinnyServlet
    extends AsyncSkinnyMicroServlet
    with AsyncSkinnyControllerBase
    with AsyncSkinnyWebPageControllerFeatures
