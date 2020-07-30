package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.duesseldorf.rrg.RRGParseResult;
import de.duesseldorf.rrg.io.RRGXMLBuilder;

import javax.swing.text.Document;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class RRGLocalWebGUI {

    public static final int port = 8001;
    static boolean hasBeenHandled = false;

    public void displayParseResults(RRGXMLBuilder rrgxmlBuilder) {
        try {

            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

            //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.createContext("/", new MyHttpHandler(rrgxmlBuilder));
            //HttpContext context = server.createContext("/");
            //context.setHandler(RRGLocalWebGUI::handleRequest);
            server.setExecutor(null);
            server.start();

            URI uri = new URI("http://localhost:" + port + "/");

            System.out.println(" Server started on port " + port + ". Open " + uri + " to view parser output");
            while (!hasBeenHandled) {
                int i = 2+3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
