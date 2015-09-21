package skinny.http.test.server.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import skinny.http.test.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class MethodHandler extends AbstractHandler {

    public abstract HttpMethod getMethod();

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            _handle(request.getMethod().equals(getMethod().name()), getMethod(), baseRequest, request, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void _handle(Boolean isAllowed, HttpMethod method, Request baseRequest, HttpServletRequest request,
                        HttpServletResponse response) throws Exception {
        if (isAllowed) {
            if (request.getRequestURI().equals("/not_found")) {
               response.setStatus(404);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/plain");
                String toReturn = request.getParameter("toReturn");
                if (toReturn != null) {
                    response.getWriter().print(toReturn);
                }
            }
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain");
            response.getWriter().print("だｍ");
        }
        baseRequest.setHandled(true);
    }

}