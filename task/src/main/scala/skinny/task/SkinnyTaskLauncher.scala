package skinny.task

trait SkinnyTaskLauncher {

  /**
    * Registered tasks.
    */
  private[this] val tasks = new scala.collection.mutable.HashMap[String, (List[String]) => Unit]

  def register(name: String, runner: (List[String]) => Unit) = tasks.update(name, runner)

  def showUsage = {
    println(
      s"""
        | Usage: sbt "task/run [task] [options...]
        |
        |${tasks.map(t => "  " + t._1).mkString("\n")}
        |
        |""".stripMargin
    )
  }

  def main(args: Array[String]) {
    args.toList match {
      case name :: parameters =>
        tasks.get(name).map(_.apply(parameters)).getOrElse(println(s"Task for ${name} not found."))
      case _ => showUsage
    }
  }

}
