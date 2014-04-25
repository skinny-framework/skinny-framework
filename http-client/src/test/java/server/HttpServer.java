package server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class HttpServer {

    private Server server;

    public HttpServer() {
    }

    public HttpServer(Handler handler, Integer port) {
        server = new Server(port);
        server.setHandler(handler);
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

}