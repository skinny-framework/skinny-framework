package server.handler;

import org.eclipse.jetty.server.Request;
import skinny.http.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PutBodyMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.PUT();
    }

    public void _handle(Boolean isAllowed, Method method,
                        Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isAllowed) {
            InputStream is = request.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            //System.out.println("Put:" + sb.toString());
            if (sb.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
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
