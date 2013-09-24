package skinny.controller.feature

import org.scalatra.ScalatraBase

trait BeforeAfterActionFeature extends ScalatraBase { self: BasicFeature with RequestScopeFeature =>

  private[this] val skinnyBeforeActions = new scala.collection.mutable.ListBuffer[() => Any]
  private[this] val skinnyAfterActions = new scala.collection.mutable.ListBuffer[() => Any]

  def beforeFilter(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    beforeAction(only, except)(action)
  }

  def beforeAction(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    skinnyBeforeActions += (
      () => {
        currentActionName.map { name =>
          val currentPathShouldBeExcluded = except.exists(_ == name)
          if (!currentPathShouldBeExcluded) {
            val allPathShouldBeIncluded = only.isEmpty
            val currentPathShouldBeIncluded = only.exists(_ == name)
            if (allPathShouldBeIncluded || currentPathShouldBeIncluded) {
              action
            }
          }
        } getOrElse action
      })
  }

  def afterFilter(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    afterAction(only, except)(action)
  }

  def afterAction(only: Seq[Symbol] = Nil, except: Seq[Symbol] = Nil)(action: => Any): Unit = {
    skinnyAfterActions += (
      () => {
        currentActionName.map { name =>
          val currentPathShouldBeExcluded = except.exists(_ == name)
          if (!currentPathShouldBeExcluded) {
            val allPathShouldBeIncluded = only.isEmpty
            val currentPathShouldBeIncluded = only.exists(_ == name)
            if (allPathShouldBeIncluded || currentPathShouldBeIncluded) {
              action
            }
          }
        } getOrElse action
      })
  }

  before() {
    skinnyBeforeActions.foreach(_.apply())
  }

  after() {
    skinnyAfterActions.foreach(_.apply())
  }

}
