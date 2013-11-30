package skinny.controller.assets

import skinny.assets.CoffeeScriptCompiler

/**
 * CoffeeScript
 */
object CoffeeScriptAssetCompiler extends AssetCompiler {
  private[this] val compiler = CoffeeScriptCompiler()

  def dir(basePath: String) = s"${basePath}/coffee"
  def extension = "coffee"
  def compile(source: String) = compiler.compile(source)
}
