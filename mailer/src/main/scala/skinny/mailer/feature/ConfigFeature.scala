package skinny.mailer.feature

import skinny.mailer.SkinnyMailerConfigApi

/**
  * Provides SkinnyMailerConfig
  */
trait ConfigFeature {

  /**
    * Returns all loaded config.
    *
    * @return config
    */
  def config: SkinnyMailerConfigApi = new SkinnyMailerConfigApi {}

}
