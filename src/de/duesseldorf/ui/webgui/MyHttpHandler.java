package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jdk.jfr.ContentType;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MyHttpHandler implements HttpHandler {

    private static String websitedir = "websitecode";
    private static Map<String, String> contentTypes = new HashMap<>();

    static {
        contentTypes.put("html", "text/html; charset=UTF-8");
        contentTypes.put("css", "text/css");
        contentTypes.put("gif", "image/gif");
        contentTypes.put("js", "text/javascript");
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("handle request: " + httpExchange.getRequestURI());

        if (!"GET".equals(httpExchange.getRequestMethod())) {
            System.out.println("request method not implemented: " + httpExchange.getRequestMethod());
        }
        handleResponse(httpExchange);
    }

    private void handleResponse(HttpExchange httpExchange) throws IOException {

        //// Find the resource
        String requestedPath = "";
        if (httpExchange.getRequestURI().getPath().equals("/")) {
            requestedPath = websitedir + "/tulipa-viewer.html";
        } else {
            requestedPath = websitedir + httpExchange.getRequestURI().getPath();
            // requestedPath =  (requestedPath.startsWith("/")) ? requestedPath.substring(1) : requestedPath;
        }
        InputStream inputStream = MyHttpHandler.class.getResourceAsStream(requestedPath);
        if (inputStream == null) {
            System.out.println("instream null, exit");
            System.exit(123);
        }

        //// ContentType
        String contentType = contentTypes.getOrDefault(requestedPath.substring(requestedPath.lastIndexOf(".")+1),"");
        httpExchange.getResponseHeaders().set("Content-Type", contentType);

        //// Process resource
        byte[] response = null;
        if (contentType.startsWith("text")){
            System.out.println("process text file" + requestedPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder htmlBuilder = new StringBuilder();

            String nextLine = null;
            while ((nextLine = reader.readLine()) != null) {
                htmlBuilder.append(nextLine.replaceAll("\\$name", "Mensch"));
                // System.out.println("readLine: " + nextLine);
            }
            String textResponse = htmlBuilder.toString();
            response = textResponse.getBytes();
        } else {
            System.out.println("process non-text file" + requestedPath);
            response = inputStream.readAllBytes();
        }


        //// send
        httpExchange.sendResponseHeaders(200, response.length);

        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

}

