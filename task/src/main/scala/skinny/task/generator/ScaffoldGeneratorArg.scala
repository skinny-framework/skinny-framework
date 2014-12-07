package skinny.task.generator

case class ScaffoldGeneratorArg(
  name: String,
  typeName: String,
  columnName: Option[String] = None)
