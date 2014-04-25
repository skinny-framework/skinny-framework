package server.handler;

import skinny.http.Method;

public class TraceMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.TRACE();
    }

}
