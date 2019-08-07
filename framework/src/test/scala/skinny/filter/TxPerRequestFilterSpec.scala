package skinny.filter

import org.scalatra.test.scalatest.ScalatraFlatSpec
import scalikejdbc.ConnectionPool
import skinny.SkinnyApiController
import skinny.routing.Routes

class TxPerRequestFilterSpec extends ScalatraFlatSpec {

  Class.forName("org.h2.Driver")
  ConnectionPool.add(Symbol("TxPerRequestFilterSpec"), "jdbc:h2:mem:TxPerRequestFilterSpec", "sa", "sa")

  object Controller extends SkinnyApiController with TxPerRequestFilter with Routes {
    override def connectionPoolForTxPerRequestFilter = ConnectionPool.get(Symbol("TxPerRequestFilterSpec"))
    def index                                        = "ok"
    get("/")(index).as(Symbol("index"))
  }
  addFilter(Controller, "/*")

  it should "be available" in {
    get("/") {
      status should equal(200)
    }
  }

}
