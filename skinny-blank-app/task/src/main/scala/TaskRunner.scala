object TaskRunner extends skinny.task.TaskLauncher {

  register("assets:precompile", (params) => {
    val buildDir = params.headOption.getOrElse("build")
    skinny.task.AssetsPrecompileTask.main(Array(buildDir))
  })
  register("routes", (params) => {
    import skinny.bootstrap._
    (new Bootstrap()).initSkinnyApp(NOOPServletContext)
    println(skinny.micro.routing.RouteRegistry.toString)
  })

  // simple example
  /*
  register("addMember", (params) => { params match  {
    case name :: _ =>
      skinny.DBSettings.initialize()
      model.Member.createWithAttributes('name -> params(0))
    case _ =>
      println("[usage] ./skinny task:run addMember Alice")
      sys.exit(1)
  }})
  */

}

