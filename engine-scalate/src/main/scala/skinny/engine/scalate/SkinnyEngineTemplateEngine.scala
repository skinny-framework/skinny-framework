package skinny.engine.scalate

import java.io.File

import org.fusesource.scalate.TemplateEngine

class SkinnyEngineTemplateEngine(
  sourceDirectories: Traversable[File] = None,
  mode: String = sys.props.getOrElse("scalate.mode", "production"))
    extends TemplateEngine(sourceDirectories, mode)