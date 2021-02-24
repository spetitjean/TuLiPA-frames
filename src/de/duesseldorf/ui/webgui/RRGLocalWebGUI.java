package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpServer;
import de.duesseldorf.rrg.io.RRGXMLBuilder;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.URI;

public class RRGLocalWebGUI {

    public static final int defaultPort = 1612;
    static boolean hasBeenHandled = false;
    private final int actualPort;
    private HttpServer server;

    public RRGLocalWebGUI(int port) {
        if (port <= 1024 || port > 65535) {
            System.out.println("trying to use a system port or nonexsiting port for GUI (" + port + ") - that is considered harmful, use port 1612 instead");
            this.actualPort = defaultPort;
        } else {
            this.actualPort = port;
        }
    }

    public void displayParseResults(String sentence, RRGXMLBuilder rrgxmlBuilder) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", actualPort), 0);

            //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.createContext("/", new MyHttpHandler(sentence, rrgxmlBuilder));
            //HttpContext context = server.createContext("/");
            //context.setHandler(RRGLocalWebGUI::handleRequest);
            server.setExecutor(null);
            server.start();

            URI uri = new URI("http://localhost:" + actualPort + "/");

            System.out.println(" Server started on port " + actualPort + ". Open " + uri + " in your web browser to view parser output");
            System.err.println(" Server started on port " + actualPort + ". Open " + uri + " in your web browser to view parser output");

            this.server = server;
	    while (true) {
                Thread.sleep(1000);
            }
	} catch (BindException be){
            System.err.println("");
            System.err.println("I cannot use port " + actualPort + " to display the parse result in your browser. You have two options:");
            System.err.println("1. Run the program again, specifying a different port with the -port commandline option (e.g. ... -port 3005 )");
            System.err.println("2. Close other programs that use the port (maybe other instances of TuLiPA?) and run the program again");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpServer getServer() {
        return server;
    }
}
