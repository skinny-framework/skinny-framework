package skinny.controller

import skinny.micro.AsyncSkinnyMicroFilter

/**
 * Skinny controller.
 */
trait AsyncSkinnyController
  extends AsyncSkinnyMicroFilter
  with AsyncSkinnyControllerBase
  with SkinnyWebPageControllerFeatures
