package skinny.orm.servlet

import org.scalatest.matchers.should.Matchers

import org.scalatest.funspec.AnyFunSpec
import scalikejdbc._

class TxPerRequestFilterSpec extends AnyFunSpec with Matchers {

  Class.forName("org.h2.Driver")
  ConnectionPool.add("TxPerRequestFilterSpec_ORM", "jdbc:h2:mem:TxPerRequestFilterSpec_ORM", "sa", "sa")

  val filter = new TxPerRequestFilter {
    override def connectionPool = ConnectionPool.get("TxPerRequestFilterSpec_ORM")
  }

//  describe("TxPerRequestFilter") {
//    it("should be available") {
//      val req     = mock[HttpServletRequest]
//      val context = mock[ServletContext]
//      val resp    = mock[HttpServletResponse]
//      val chain   = mock[FilterChain]
//      when(req.getServletContext).thenReturn(context)
//      when(req.getRequestURI).thenReturn("/")
//      when(context.getContextPath).thenReturn("/")
//      filter.doFilter(req, resp, chain)
//    }
//  }

}
