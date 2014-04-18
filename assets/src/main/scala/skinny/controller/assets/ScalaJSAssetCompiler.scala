package skinny.controller.assets

/**
 * Scala.js source code provider.
 */
object ScalaJSAssetCompiler extends AssetCompiler {
  def dir(basePath: String) = s"${basePath}/scala"
  def extension = "scala"
  def compile(path: String, source: String) = throw new UnsupportedOperationException("Use ./skinny scalajs:watch instead.")
}
