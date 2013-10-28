package controller

import org.scalatra.test.scalatest._
import skinny._
import skinny.controller._
import skinny.test.SkinnyTestSupport

class AssetsSpec extends ScalatraFlatSpec with SkinnyTestSupport {

  addFilter(AssetsController, "/*")

  it should "show coffee script resources" in {
    get("/assets/js/echo.js") {
      status should equal(200)
      body should equal("""(function() {
  var echo;

  echo = function(v) {
    return console.log(v);
  };

  echo("foo");

}).call(this);
""")
    }
  }

  it should "show less resources" in {
    get("/assets/css/box.css") {
      status should equal(200)
      body should equal(""".box {
  color: #fe33ac;
  border-color: #fdcdea;
}
""")
    }
  }

}
