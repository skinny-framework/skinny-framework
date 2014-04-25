package server.handler;

import org.eclipse.jetty.server.Request;
import skinny.http.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PostMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.POST();
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
