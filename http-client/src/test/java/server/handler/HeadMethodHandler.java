package server.handler;

import skinny.http.Method;

public class HeadMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.HEAD();
    }

}