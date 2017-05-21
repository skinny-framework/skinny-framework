package skinny.injection

/**
  * Skinny Framework's components.
  */
class SkinnyModule extends scaldi.Module {

  bind[skinny.SkinnyEnv] to skinny.SkinnyEnv

  bind[skinny.SkinnyConfig] to skinny.SkinnyConfig

}
