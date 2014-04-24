package server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PostFormdataServer extends HttpServer {

    private Integer port = 8888;

    public PostFormdataServer(Integer port) {
       this.port = port;
    }

    ServletContextHandler handler = new ServletContextHandler();

    public static class FileuploadServlet extends HttpServlet {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            if (ServletFileUpload.isMultipartContent(req)) {

                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1426);
                factory.setRepository(new File("target"));

                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setSizeMax(20 * 1024);
                upload.setFileSizeMax(10 * 1024);

                String toResponse = "FAILURE!!!";
                try {
                    List<FileItem> items = upload.parseRequest(req);
                    for (FileItem item : items) {
                        if ("toResponse".equals(item.getFieldName())) {
                            toResponse = item.getString();
                        }
                        String result = item.getFieldName() + "(" + item.getContentType() + ") -> " + item.getString("UTF-8");
                        //System.out.println(result);
                    }
                } catch (FileUploadException e) {
                    e.printStackTrace();
                }
                resp.getWriter().write(toResponse);
            }
        }
    }

    private Server server;

    public void start() throws Exception {
        handler.addServlet(new ServletHolder(new FileuploadServlet()), "/*");
        server = new Server(this.port);
        server.setHandler(handler);
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }

}
