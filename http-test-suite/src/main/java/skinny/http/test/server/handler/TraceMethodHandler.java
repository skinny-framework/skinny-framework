package skinny.http.test.server.handler;

import skinny.http.test.HttpMethod;

public class TraceMethodHandler extends MethodHandler {

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.TRACE();
    }

}
