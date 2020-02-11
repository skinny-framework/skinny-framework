package skinny.controller.feature

import skinny.micro.async.AsyncBeforeAfterDsl
import skinny.micro._

/**
  * beforeAction/afterAction support.
  *
  * @see http://guides.rubyonrails.org/action_controller_overview.html
  */
trait AsyncBeforeAfterActionFeature extends SkinnyMicroBase with AsyncBeforeAfterDsl {

  self: Handler with ActionDefinitionFeature =>

  /**
    * Collection of beforeAction functions.
    */
  private[this] val skinnyBeforeActions = new scala.collection.mutable.ListBuffer[(Context) => Any]

  /**
    * Collection of afterAction functions.
    */
  private[this] val skinnyAfterActions = new scala.collection.mutable.ListBuffer[(Context) => Any]

  /**
    * If you prefer #beforeFilter than #beforeAction, keep going!
    */
  def beforeFilter(only: Seq[String] = Nil, except: Seq[String] = Nil)(action: (Context) => Any): Unit = {
    beforeAction(only, except)(action)
  }

  /**
    * Registers beforeAction to this controller.
    *
    * @param only this action should be applied only for these action methods
    * @param except this action should not be applied for these action methods
    * @param action action
    */
  def beforeAction(only: Seq[String] = Nil, except: Seq[String] = Nil)(action: (Context) => Any): Unit = {
    skinnyBeforeActions += (
        (ctx) => {
          currentActionName.map { name =>
            // current path should not be excluded
            if (!except.exists(_ == name)) {
              // all actions should be included or this action should be included
              if (only.isEmpty || only.exists(_ == name)) {
                action(ctx)
              }
            }
          } getOrElse ().asInstanceOf[Any]
        }
    )
  }

  /**
    * If you prefer #afterFilter than #afterAction, keep going!
    */
  def afterFilter(only: Seq[String] = Nil, except: Seq[String] = Nil)(action: (Context) => Any): Unit = {
    afterAction(only, except)(action)
  }

  /**
    * Registers afterAction to this controller.
    *
    * @param only this action should be applied only for these action methods
    * @param except this action should not be applied for these action methods
    * @param action action
    */
  def afterAction(only: Seq[String] = Nil, except: Seq[String] = Nil)(action: (Context) => Any): Unit = {
    skinnyAfterActions += (
        (ctx) => {
          currentActionName.map { name =>
            // current path should not be excluded
            if (!except.exists(_ == name)) {
              // all actions should be included or this action should be included
              if (only.isEmpty || only.exists(_ == name)) {
                action(ctx)
              }
            }
          } getOrElse ().asInstanceOf[Any]
        }
    )
  }

  // executing actions in this controller

  before() { (ctx) =>
    skinnyBeforeActions.foreach(_.apply(ctx))
  }

  after() { (ctx) =>
    skinnyAfterActions.foreach(_.apply(ctx))
  }

}
