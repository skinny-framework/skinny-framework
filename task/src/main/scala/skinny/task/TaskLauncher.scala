package skinny.task

/**
 * Task launcher.
 */
trait TaskLauncher {

  private[this] val tasks = new scala.collection.mutable.ListBuffer[(String, (List[String]) => Unit)]

  // built-in tasks
  register("scaffold", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:ssp", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:scaml", (params) => ScaffoldSspGenerator.run(params))
  register("scaffold:jade", (params) => ScaffoldSspGenerator.run(params))

  def register(name: String, runner: (List[String]) => Unit) = tasks.append(name -> runner)

  def showUsage = {
    println("Usage: sbt \"task/run scaffold name:String birthday:Option[LocalDate]\"")
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
