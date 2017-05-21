package skinny.mailer.feature

import skinny.mailer.SkinnyMailerExtraConfig

/**
  * Provides extra properties to SkinnyConfig.
  */
trait ExtraConfigFeature {

  /**
    * Extra properties.
    *
    * @return config
    */
  def extraConfig: SkinnyMailerExtraConfig = SkinnyMailerExtraConfig()

}
