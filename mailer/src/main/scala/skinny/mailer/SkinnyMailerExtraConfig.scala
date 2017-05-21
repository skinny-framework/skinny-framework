package skinny.mailer

/**
  * Key value config.
  */
object SkinnyMailerExtraConfig {

  def apply(keyValues: (String, Any)*): SkinnyMailerExtraConfig = new SkinnyMailerExtraConfig(keyValues.toMap)

}

/**
  * Key value config.
  */
case class SkinnyMailerExtraConfig(properties: Map[String, Any])
