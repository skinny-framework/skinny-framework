package skinny.http.test

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

package object server {
  type ServerBehavior = (HttpServletRequest, HttpServletResponse) => Unit
}
