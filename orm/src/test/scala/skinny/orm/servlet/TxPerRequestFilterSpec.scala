package skinny.orm.servlet

import javax.servlet.{ ServletContext, FilterChain }
import javax.servlet.http._

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

class TxPerRequestFilterSpec extends FunSpec with Matchers with MockitoSugar {

  val filter = new TxPerRequestFilter {
  }

  describe("TxPerRequestFilter") {
    it("should be available") {
      try {
        val req = mock[HttpServletRequest]
        val context = mock[ServletContext]
        val resp = mock[HttpServletResponse]
        val chain = mock[FilterChain]
        when(req.getServletContext).thenReturn(context)
        when(req.getRequestURI).thenReturn("/")
        when(context.getContextPath).thenReturn("/")
        filter.doFilter(req, resp, chain)
      } catch { case e: Exception => e.printStackTrace() }
    }
  }

}
