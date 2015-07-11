package org.scalatra.test.scalatest

import org.scalatest._
import org.scalatest.junit.{ JUnit3Suite, JUnitSuite }

/**
 * Convenience trait to add Scalatra test support to JUnit3Suite.
 */
trait ScalatraJUnit3Suite
  extends JUnit3Suite
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to JUnitSuite.
 */
trait ScalatraJUnitSuite
  extends JUnitSuite
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to FeatureSpec.
 */
trait ScalatraFeatureSpec
  extends FeatureSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to Spec.
 */
trait ScalatraSpec
  extends FunSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to FlatSpec.
 */
trait ScalatraFlatSpec
  extends FlatSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to FunSpec.
 */
trait ScalatraFunSpec
  extends FunSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to FreeSpec.
 */
trait ScalatraFreeSpec
  extends FreeSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to WordSpec.
 */
trait ScalatraWordSpec
  extends WordSpecLike
  with ScalatraSuite

/**
 * Convenience trait to add Scalatra test support to FunSuite.
 */
trait ScalatraFunSuite
  extends FunSuite
  with ScalatraSuite
