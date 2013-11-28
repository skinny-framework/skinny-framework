package skinny.controller

import skinny.assets.CoffeeScriptCompiler

object CoffeeScriptAssetCompiler extends AssetCompiler {
  private[this] val compiler = CoffeeScriptCompiler()

  def dir(basePath: String) = s"${basePath}/coffee"
  def extension = "coffee"
  def compile(source: String) = compiler.compile(source)

}
