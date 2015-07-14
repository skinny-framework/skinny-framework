package skinny.injection

import scaldi.{ Injector, MutableInjectorAggregation, Module }
import skinny.{ SkinnyConfig, SkinnyEnv }
import scala.collection.concurrent.TrieMap
import scala.util.control.NonFatal

/**
 * The singleton holder of Scaldi components for SkinnyEnv.
 */
object SkinnyScaldi extends SkinnyScaldi {

  private[this] val scaldiInjectors: TrieMap[String, MutableInjectorAggregation] = TrieMap()

  /**
   * Finds modules for this SkinnyEnv.
   */
  def modulesForEnv(env: String = SkinnyEnv.getOrElse(SkinnyEnv.Development), defaultModules: Seq[Module] = Nil): MutableInjectorAggregation = {
    scaldiInjectors.getOrElseUpdate(env, {
      new MutableInjectorAggregation(
        defaultModules.toList ::: SkinnyConfig.stringSeqConfigValue("scaldi.modules").map { moduleClasses: Seq[String] =>
          moduleClasses.map { moduleClass: String =>
            try {
              val clazz: Class[_] = Class.forName(moduleClass)
              clazz.newInstance().asInstanceOf[Module]
            } catch {
              case NonFatal(e) =>
                throw new ScaldiConfigException(
                  s"Failed to load a Scaldi module because of ${e.getClass.getCanonicalName} (${e.getMessage})", e)
            }
          }.toList
        }.getOrElse(Nil))
    })
  }

}

/**
 * Scaldi support.
 */
trait SkinnyScaldi {

  /**
   * Module for framework components.
   */
  def skinnyModule: Module = new SkinnyModule

  def injectorForEnv(env: String = SkinnyEnv.getOrElse(SkinnyEnv.Development)): Injector = {
    SkinnyScaldi.modulesForEnv(env, Seq(skinnyModule))
  }

}
