package skinny.http

/**
 * Request body from a text value.
 */
case class TextInput(textBody: String, charset: String = HTTP.DEFAULT_CHARSET)

/**
 * No text input
 */
object NoTextInput extends TextInput(null, null)
