package skinny.orm.servlet

import javax.servlet.{ FilterChain, ServletContext }
import javax.servlet.http._

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import scalikejdbc._

class TxPerRequestFilterSpec extends FunSpec with Matchers with MockitoSugar {

  Class.forName("org.h2.Driver")
  ConnectionPool.add("TxPerRequestFilterSpec_ORM", "jdbc:h2:mem:TxPerRequestFilterSpec_ORM", "sa", "sa")

  val filter = new TxPerRequestFilter {
    override def connectionPool = ConnectionPool.get("TxPerRequestFilterSpec_ORM")
  }

  describe("TxPerRequestFilter") {
    it("should be available") {
      val req     = mock[HttpServletRequest]
      val context = mock[ServletContext]
      val resp    = mock[HttpServletResponse]
      val chain   = mock[FilterChain]
      when(req.getServletContext).thenReturn(context)
      when(req.getRequestURI).thenReturn("/")
      when(context.getContextPath).thenReturn("/")
      filter.doFilter(req, resp, chain)
    }
  }

}
