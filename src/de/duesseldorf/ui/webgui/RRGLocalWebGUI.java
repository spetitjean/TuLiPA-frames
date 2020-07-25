package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class RRGLocalWebGUI {

    static boolean hasBeenHandled = false;

    public void displayParseResults() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);

            //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
            server.createContext("/", new MyHttpHandler());
            //HttpContext context = server.createContext("/");
            //context.setHandler(RRGLocalWebGUI::handleRequest);
            server.setExecutor(null);
            server.start();


            System.out.println(" Server started on port 8001");
            while (!hasBeenHandled) {
                int i = 2+3;
            }
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("http://localhost:8001/");
            // URI uri = new URI("https://www.duckduckgo.com/");
            desktop.browse(uri);

        } catch (Exception e) {
            e.printStackTrace();
        }


        // try {
            /*
            ServerSocket server = new ServerSocket(3030);
            boolean x = true;

            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("http://localhost:3030");
            // URI uri = new URI("https://www.duckduckgo.com/");
            desktop.browse(uri);

            while (x) {
                Socket client = null;

                try {
                    client = server.accept();
                    new PrintWriter(client.getOutputStream(), true).println("huhu");

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (client != null)
                        try {
                            client.close();
                            x = false;
                        } catch (IOException e) {
                        }
                }

            }
        } catch (Exception e){

        }*/
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String response = "Hi there!";
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        hasBeenHandled = true;
    }
}
