package server.handler;

import skinny.http.Method;

public class GetMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.GET();
    }

}
