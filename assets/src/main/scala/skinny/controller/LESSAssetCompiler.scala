package skinny.controller

import skinny.assets.{ LessCompiler, CoffeeScriptCompiler }

object LessAssetCompiler extends AssetCompiler {
  private[this] val compiler = LessCompiler

  def dir(basePath: String) = s"${basePath}/less"
  def extension = "less"
  def compile(source: String) = compiler.compile(source)

}
