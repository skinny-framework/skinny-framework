package skinny

/**
 * Response body format.
 */
trait Format {
  val name: String
  val contentType: String
}

/**
 * Response body formats.
 */
object Format {

  def apply(format: String): Format = format match {
    case HTML.name => HTML
    case XML.name => XML
    case JSON.name => JSON
    case JavaScript.name => JavaScript
    case _ => HTML
  }

  case object HTML extends Format {
    override val name = "html"
    override val contentType = "text/html"
  }

  case object XML extends Format {
    override val name = "xml"
    override val contentType = "application/xml"
  }

  case object JSON extends Format {
    override val name = "json"
    override val contentType = "application/json"
  }

  case object JavaScript extends Format {
    override val name = "js"
    override val contentType = "application/javascript"
  }

}
