package server.handler;

import skinny.http.Method;

public class DeleteMethodHandler extends MethodHandler {

    @Override
    public Method getMethod() {
        return Method.DELETE();
    }

}
