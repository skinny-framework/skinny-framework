package controller

import skinny.test.scalatest.SkinnyFlatSpec

class CustomLayoutControllerSpec extends SkinnyFlatSpec with unit.SkinnyTesting {

  addFilter(Controllers.customLayout, "/*")

  it should "show index" in {
    get("/custom-layout/") {
      status should equal(200)
      body should include("foo")
      body should include("index")
    }
  }

  it should "show default" in {
    get("/custom-layout/default") {
      status should equal(200)
      body should not include ("foo")
      body should include("日本語")
      body should include("ddd")
    }
  }

  it should "show bar" in {
    get("/custom-layout/bar") {
      status should equal(200)
      body should not include ("foo")
      body should not include ("日本語")
      body should include("!!!bar!!!")
      body should include("BBB")
    }
  }

}
