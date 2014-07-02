package skinny.controller.feature

import scaldi._
import skinny.{ SkinnyConfig, SkinnyEnv }
import skinny.controller.SkinnyControllerBase

trait ScaldiFeature extends Injectable { self: SkinnyControllerBase =>

  def skinnyModule: Module = new Module {
    bind[SkinnyEnv] to SkinnyEnv
    bind[SkinnyConfig] to SkinnyConfig
  }
  def scaldiModules: Seq[Module]

  implicit lazy val skinnyControllerFeatureScaldiInjector: Injector =
    new MutableInjectorAggregation(skinnyModule :: scaldiModules.toList)

}
