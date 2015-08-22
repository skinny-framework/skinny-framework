package skinny.controller.feature

import skinny.micro.{ Handler, SkinnyMicroBase }
import skinny.micro.base.BeforeAfterDsl

/**
 * beforeAction/afterAction support.
 *
 * @see http://guides.rubyonrails.org/action_controller_overview.html
 */
trait BeforeAfterActionFeature
    extends SkinnyMicroBase
    with BeforeAfterDsl {

  self: Handler with ActionDefinitionFeature with RequestScopeFeature =>

  /**
   * Collection of beforeAction functions.
   */
  private[this] val skinnyBeforeActions = new scala.collection.mutable.ListBuffer[() => Any]

  /**
   * Collection of afterAction functions.
   */
  private[this] val skinnyAfterActions = new scala.collection.mutable.ListBuffer[() => Any]

  /**
   * If you prefer #beforeFilter than #beforeAction, keep going!
   */
  def beforeFilter(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    beforeAction(only, except)(action)
  }

  /**
   * Registers beforeAction to this controller.
   *
   * @param only this action should be applied only for these action methods
   * @param except this action should not be applied for these action methods
   * @param action action
   */
  def beforeAction(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    skinnyBeforeActions += (
      () => {
        currentActionName.map { name =>
          // current path should not be excluded
          if (!except.exists(_ == name)) {
            // all actions should be included or this action should be included
            if (only.isEmpty || only.exists(_ == name)) {
              action
            }
          }
        } getOrElse ().asInstanceOf[Any]
      })
  }

  /**
   * If you prefer #afterFilter than #afterAction, keep going!
   */
  def afterFilter(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    afterAction(only, except)(action)
  }

  /**
   * Registers afterAction to this controller.
   *
   * @param only this action should be applied only for these action methods
   * @param except this action should not be applied for these action methods
   * @param action action
   */
  def afterAction(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    skinnyAfterActions += (
      () => {
        currentActionName.map { name =>
          // current path should not be excluded
          if (!except.exists(_ == name)) {
            // all actions should be included or this action should be included
            if (only.isEmpty || only.exists(_ == name)) {
              action
            }
          }
        } getOrElse ().asInstanceOf[Any]
      })
  }

  // executing actions in this controller

  before() {
    skinnyBeforeActions.foreach(_.apply())
  }

  after() {
    skinnyAfterActions.foreach(_.apply())
  }

}
