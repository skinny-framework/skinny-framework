package skinny.task

/**
 * Task launcher.
 */
trait TaskLauncher {

  private[this] val tasks = new scala.collection.mutable.ListBuffer[(String, (List[String]) => Unit)]

  // built-in tasks
  register("controller", (params) => ControllerGenerator.run(params))
  register("model", (params) => ModelGenerator.run(params))
  register("scaffold", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:ssp", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:scaml", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:jade", (params) => ScaffoldSspGenerator.run(params))

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
