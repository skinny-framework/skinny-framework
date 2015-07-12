package skinny.test.scalatest

import org.scalatest._
import org.scalatest.junit.{ JUnit3Suite, JUnitSuite }
import skinny.engine.test.scalatest.SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to JUnit3Suite.
 */
trait SkinnyJUnit3Suite
  extends JUnit3Suite
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to JUnitSuite.
 */
trait SkinnyJUnitSuite
  extends JUnitSuite
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to FeatureSpec.
 */
trait SkinnyFeatureSpec
  extends FeatureSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to Spec.
 */
trait SkinnySpec
  extends FunSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to FlatSpec.
 */
trait SkinnyFlatSpec
  extends FlatSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to FunSpec.
 */
trait SkinnyFunSpec
  extends FunSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to FreeSpec.
 */
trait SkinnyFreeSpec
  extends FreeSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to WordSpec.
 */
trait SkinnyWordSpec
  extends WordSpecLike
  with SkinnyEngineSuite

/**
 * Convenience trait to add Skinny test support to FunSuite.
 */
trait SkinnyFunSuite
  extends FunSuite
  with SkinnyEngineSuite
