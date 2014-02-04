package skinny.controller.assets

import skinny.assets.ReactJSXCompiler

/**
 * React JSX Template
 */
object ReactJSXAssetCompiler extends AssetCompiler {
  private[this] val compiler = new ReactJSXCompiler

  def dir(basePath: String) = s"${basePath}/jsx"
  def extension = "jsx"
  def compile(path: String, source: String) = compiler.compile(path, source)
}
