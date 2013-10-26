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
  type SkinnyServlet = skinny.controller.SkinnyServlet

  type Params = skinny.controller.Params
  type Flash = skinny.controller.Flash
  type Routes = skinny.routing.Routes

  type SkinnyCRUDMapper[A] = skinny.orm.SkinnyCRUDMapper[A]
  type SkinnyMapper[A] = skinny.orm.SkinnyMapper[A]
  type SkinnyJoinTable[A] = skinny.orm.SkinnyJoinTable[A]

}
