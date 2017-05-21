package skinny.controller.assets

import skinny.assets.LessCompiler

/**
  * LESS
  */
object LessAssetCompiler extends AssetCompiler {
  private[this] val compiler = LessCompiler

  def dir(basePath: String)                 = s"${basePath}/less"
  def extension                             = "less"
  def compile(path: String, source: String) = compiler.compile(path, source)
}
