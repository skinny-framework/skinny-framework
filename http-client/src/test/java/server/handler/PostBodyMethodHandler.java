package server.handler;

import org.eclipse.jetty.server.Request;
import skinny.http.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PostBodyMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.POST();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (request.getMethod().equals(getMethod().toString())) {
                InputStream is = request.getInputStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line);
                }
                //System.out.println("POST:" + sb.toString());

                response.setStatus(HttpServletResponse.SC_OK);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(sb.toString());
                baseRequest.setHandled(true);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
