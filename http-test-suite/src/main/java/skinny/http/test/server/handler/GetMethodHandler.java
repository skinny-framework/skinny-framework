package skinny.http.test.server.handler;

import skinny.http.test.HttpMethod;

public class GetMethodHandler extends MethodHandler {

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET();
    }

}
