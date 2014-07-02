package skinny.controller.feature

import scaldi._
import skinny.controller.SkinnyControllerBase
import skinny.injection.SkinnyScaldi

/**
 * Scaldi support for SkinnyController.
 */
trait ScaldiFeature extends Injectable with SkinnyScaldi { self: SkinnyControllerBase =>

  /**
   * Overriden Scaldi modules.
   */
  def scaldiModules: Seq[Module] = Nil

  /**
   * Implicit value for scaldi.Injector.
   */
  implicit lazy val skinnyControllerFeatureScaldiInjector: Injector = {
    if (scaldiModules.isEmpty) injectorForEnv()
    else new MutableInjectorAggregation(skinnyModule :: scaldiModules.toList)
  }

}
