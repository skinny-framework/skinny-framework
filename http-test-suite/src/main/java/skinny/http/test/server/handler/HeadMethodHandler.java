package skinny.http.test.server.handler;

import skinny.http.test.HttpMethod;

public class HeadMethodHandler extends MethodHandler {

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.HEAD();
    }

}