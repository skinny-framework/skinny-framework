package skinny.http

/**
 * Body from a file.
 */
case class FileInput(file: java.io.File, contentType: String)

/**
 * No file input.
 */
object NoFileInput extends FileInput(null, null)
