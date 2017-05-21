import skinny.micro.implicits
import skinny.micro.implicits.TypeConverterSupport

/**
  * Skinny framework for rapid web app development in Scala.
  *
  * Skinny is a full-stack web app framework, which is built on Scalatra and additional components are integrated.
  * To put it simply, Skinny framework's concept is Scala on Rails. Skinny is highly inspired by Ruby on Rails and it is optimized for sustainable productivity for ordinary Servlet-based app development.
  */
package object skinny {

  type Format = skinny.micro.Format
  val Format = skinny.micro.Format

  type SkinnyLifeCycle = bootstrap.SkinnyLifeCycle
  type ServletContext  = javax.servlet.ServletContext

  type SkinnyControllerBase = skinny.controller.SkinnyControllerBase
  type SkinnyController     = skinny.controller.SkinnyController
  type SkinnyApiController  = skinny.controller.SkinnyApiController

  type SkinnyResource           = skinny.controller.SkinnyResource
  type SkinnyResourceWithId[Id] = skinny.controller.SkinnyResourceWithId[Id]

  type SkinnyServlet    = skinny.controller.SkinnyServlet
  type SkinnyApiServlet = skinny.controller.SkinnyApiServlet

  type Params = skinny.controller.Params
  val Params = skinny.controller.Params

  type MultiParams = skinny.controller.MultiParams
  val MultiParams = skinny.controller.MultiParams

  type Flash = skinny.controller.Flash
  val Flash = skinny.controller.Flash

  type KeyAndErrorMessages = skinny.controller.KeyAndErrorMessages
  val KeyAndErrorMessages = skinny.controller.KeyAndErrorMessages

  type Routes = skinny.routing.Routes

  type SkinnyNoIdMapper[A] = skinny.orm.SkinnyNoIdMapper[A]

  type SkinnyCRUDMapper[A]           = skinny.orm.SkinnyCRUDMapper[A]
  type SkinnyCRUDMapperWithId[Id, A] = skinny.orm.SkinnyCRUDMapperWithId[Id, A]

  type SkinnyMapper[A]           = skinny.orm.SkinnyMapper[A]
  type SkinnyMapperWithId[Id, A] = skinny.orm.SkinnyMapperWithId[Id, A]

  type SkinnyJoinTable[A] = skinny.orm.SkinnyJoinTable[A]

  type TypeConverter[A, B]  = skinny.micro.implicits.TypeConverter[A, B]
  type TypeConverterSupport = skinny.micro.implicits.TypeConverterSupport
  val TypeConverterSupport = implicits.TypeConverterSupport

  type Logging        = skinny.logging.Logging
  type LoggerProvider = skinny.logging.LoggerProvider

  type Context = skinny.micro.Context

}
