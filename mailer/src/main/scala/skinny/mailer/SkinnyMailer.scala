package skinny.mailer

import skinny.mailer.feature._
import skinny.mailer.implicits.SkinnyMailerImplicits

object SkinnyMailer {

  def apply(mailerConfig: SkinnyMailerConfig): SkinnyMailer = new SkinnyMailer {
    override def config = mailerConfig
  }

  def apply(smConfig: SkinnyMailerConfig, extra: SkinnyMailerExtraConfig): SkinnyMailer = new SkinnyMailer {
    override def config = smConfig
    override def extraConfig = extra
  }

}

/**
 * SkinnyMailer
 */
trait SkinnyMailer
  extends SkinnyMailerBase
  with ConfigFeature
  with SmtpConfigFeature
  with ExtraConfigFeature
  with JavaMailSessionFeature
  with MessageBuilderFeature
  with SkinnyMailerImplicits
