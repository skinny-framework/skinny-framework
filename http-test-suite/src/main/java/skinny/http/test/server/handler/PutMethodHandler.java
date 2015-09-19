package skinny.http.test.server.handler;

import org.eclipse.jetty.server.Request;
import skinny.http.test.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutMethodHandler extends MethodHandler {

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.PUT();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getMethod().equals(getMethod().toString())) {
                String userName = request.getParameter("userName");
                String result = "userName:" + userName;
                //System.out.println(result);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(result);
                baseRequest.setHandled(true);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
