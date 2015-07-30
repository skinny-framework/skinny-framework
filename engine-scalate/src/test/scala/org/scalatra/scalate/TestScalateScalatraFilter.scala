package org.scalatra.scalate

import skinny.engine.SkinnyEngineFilter
import skinny.engine.scalate.ScalateSupport

// The "test" is that this compiles, to avoid repeats of defects like Issue #9.
class TestScalateScalatraFilter extends SkinnyEngineFilter with ScalateSupport
