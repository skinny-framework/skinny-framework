package skinny.controller

import skinny.assets._

object SassAssetCompiler extends AssetCompiler {
  private[this] val compiler = SassCompiler

  def dir(basePath: String) = s"${basePath}/sass"
  def extension = "sass"
  def compile(source: String) = compiler.compileIndented(source)

}
