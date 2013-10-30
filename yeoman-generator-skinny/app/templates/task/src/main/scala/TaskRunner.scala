object TaskLancher extends skinny.task.TaskLauncher {

  register("assets:precompile", (params) => skinny.task.AssetsPrecompileTask.main(Array("build")))

}

