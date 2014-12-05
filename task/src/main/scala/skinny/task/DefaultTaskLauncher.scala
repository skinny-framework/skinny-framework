package skinny.task

import skinny.task.generator._
import skinny.dbmigration.DBMigration
import skinny.SkinnyEnv

/**
 * Task launcher.
 */
trait DefaultTaskLauncher extends SkinnyTaskLauncher {

  // built-in tasks
  register("generate:controller", (params) => ControllerGenerator.run(params))
  register("generate:model", (params) => ModelGenerator.run(params))
  register("generate:migration", (params) => DBMigrationFileGenerator.run(params))
  register("generate:scaffold", (params) => ScaffoldSspGenerator.run(params))
  register("generate:scaffold:ssp", (params) => ScaffoldSspGenerator.run(params))
  register("generate:scaffold:scaml", (params) => ScaffoldScamlGenerator.run(params))
  register("generate:scaffold:jade", (params) => ScaffoldJadeGenerator.run(params))
  register("generate:reverse-model", (params) => ReverseModelGenerator.run(params))
  register("generate:reverse-scaffold", (params) => ReverseScaffoldGenerator.run("ssp", params))
  register("generate:reverse-scaffold:ssp", (params) => ReverseScaffoldGenerator.run("ssp", params))
  register("generate:reverse-scaffold:scaml", (params) => ReverseScaffoldGenerator.run("scaml", params))
  register("generate:reverse-scaffold:jade", (params) => ReverseScaffoldGenerator.run("jade", params))

  register("db:migrate", {
    case env :: dbName :: rest => DBMigration.migrate(env, dbName)
    case params => DBMigration.migrate(params.headOption.getOrElse(SkinnyEnv.Development))
  })
  register("db:repair", {
    case env :: dbName :: rest => DBMigration.repair(env, dbName)
    case params => DBMigration.repair(params.headOption.getOrElse(SkinnyEnv.Development))
  })

}

