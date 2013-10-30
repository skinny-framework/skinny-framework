package skinny.task

/**
 * Task launcher.
 */
trait TaskLauncher {

  private[this] val tasks = new scala.collection.mutable.ListBuffer[(String, (List[String]) => Unit)]

  // built-in tasks
  register("generate-controller", (params) => ControllerGenerator.run(params))
  register("generate-model", (params) => ModelGenerator.run(params))
  register("generate-scaffold", (params) => ScaffoldSspGenerator.run(params))
  register("generate-scaffold:ssp", (params) => ScaffoldSspGenerator.run(params))
  register("genarate-scaffold:scaml", (params) => ScaffoldSspGenerator.run(params))
  register("generate-scaffold:jade", (params) => ScaffoldSspGenerator.run(params))

  def register(name: String, runner: (List[String]) => Unit) = tasks.append(name -> runner)

  def showUsage = {
    println(
      s"""
        | Usage: sbt "task/run [task] [options...]
        |
        |${tasks.map(t => "  " + t._1).mkString("\n")}
        |
        |""".stripMargin)
  }

  def main(args: Array[String]) {
    args.toList match {
      case name :: parameters =>
        tasks.find(t => t._1 == name).map { case (_, f) => f.apply(parameters) }
          .getOrElse { println(s"Task for ${name} not found.") }
      case _ => showUsage
    }
  }

}
