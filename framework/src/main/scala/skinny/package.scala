/**
 * Skinny framework for rapid web app development in Scala.
 *
 * Skinny is a full-stack web app framework, which is built on Scalatra and additional components are integrated.
 * To put it simply, Skinny framework's concept is Scala on Rails. Skinny is highly inspired by Ruby on Rails and it is optimized for sustainable productivity for ordinary Servlet-based app development.
 */
package object skinny {

  type SkinnyLifeCycle = bootstrap.SkinnyLifeCycle
  type ServletContext = javax.servlet.ServletContext

  type SkinnyControllerBase = skinny.controller.SkinnyControllerBase
  type SkinnyController = skinny.controller.SkinnyController

  type SkinnyResource = skinny.controller.SkinnyResource
  type SkinnyResourceWithId[Id] = skinny.controller.SkinnyResourceWithId[Id]

  type SkinnyServlet = skinny.controller.SkinnyServlet

  type Params = skinny.controller.Params
  val Params = skinny.controller.Params

  type MultiParams = skinny.controller.MultiParams
  val MultiParams = skinny.controller.MultiParams

  type Flash = skinny.controller.Flash
  val Flash = skinny.controller.Flash

  type KeyAndErrorMessages = skinny.controller.KeyAndErrorMessages
  val KeyAndErrorMessages = skinny.controller.KeyAndErrorMessages

  type Routes = skinny.routing.Routes

  type SkinnyCRUDMapper[A] = skinny.orm.SkinnyCRUDMapper[A]
  type SkinnyCRUDMapperWithId[Id, A] = skinny.orm.SkinnyCRUDMapperWithId[Id, A]

  type SkinnyMapper[A] = skinny.orm.SkinnyMapper[A]
  type SkinnyMapperWithId[Id, A] = skinny.orm.SkinnyMapperWithId[Id, A]

  type SkinnyJoinTable[A] = skinny.orm.SkinnyJoinTable[A]
  type SkinnyJoinTableWithId[Id, A] = skinny.orm.SkinnyJoinTableWithId[Id, A]

  type TypeConverter[A, B] = org.scalatra.util.conversion.TypeConverter[A, B]

  type Logging = skinny.logging.Logging

}
