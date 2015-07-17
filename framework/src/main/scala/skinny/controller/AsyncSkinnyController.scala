package skinny.controller

import skinny.engine.AsyncSkinnyEngineFilter

/**
 * Skinny controller.
 */
trait AsyncSkinnyController
  extends AsyncSkinnyEngineFilter
  with AsyncSkinnyControllerBase
  with SkinnyWebPageControllerFeatures
