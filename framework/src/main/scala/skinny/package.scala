package object skinny {

  type SkinnyLifeCycle = bootstrap.SkinnyLifeCycle
  type ServletContext = javax.servlet.ServletContext

  type SkinnyControllerBase = skinny.controller.SkinnyControllerBase
  type SkinnyController = skinny.controller.SkinnyController
  type SkinnyResource = skinny.controller.SkinnyResource
  type SkinnyServlet = skinny.controller.SkinnyServlet

  type Params = skinny.controller.Params
  type Routes = skinny.routing.Routes

  type SkinnyCRUDMapper[A] = skinny.orm.SkinnyCRUDMapper[A]
  type SkinnyMapper[A] = skinny.orm.SkinnyMapper[A]
  type SkinnyJoinTable[A] = skinny.orm.SkinnyJoinTable[A]

  type StrongParameters = skinny.orm.StrongParameters
  val StrongParameters = skinny.orm.StrongParameters

  type PermittedStrongParameters = skinny.orm.PermittedStrongParameters

  type ParamType = skinny.orm.ParamType
  val ParamType = skinny.orm.ParamType

}