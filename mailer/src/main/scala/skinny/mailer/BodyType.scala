package skinny.mailer

/**
 * Represents body type (text or html)
 */
sealed trait BodyType {
  def extension: String
}

/**
 * plain text
 */
case object Text extends BodyType {
  override val extension = "text"
}

/**
 * HTML
 */
case object Html extends BodyType {
  override val extension = "html"
}
