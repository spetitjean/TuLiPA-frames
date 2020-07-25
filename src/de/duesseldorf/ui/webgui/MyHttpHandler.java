package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class MyHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("got request: " + httpExchange.getRequestURI());


        String requestParamValue = null;
        if ("GET".equals(httpExchange.getRequestMethod())) {
            requestParamValue = "bla";
            System.out.println("request for: " + requestParamValue);
        } /* else if ("POST".equals(httpExchange)) {
                requestParamValue = handlePostRequest(httpExchange);
            }*/
        handleResponse(httpExchange, requestParamValue);
    }


    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange.getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }


    private void handleResponse(HttpExchange httpExchange, String requestParamValue) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        String websitedir = "websitecode";
        InputStream inputStream = null;
        if (httpExchange.getRequestURI().getPath().equals("/")) {
            inputStream = MyHttpHandler.class.getResourceAsStream(websitedir + "/tulipa-viewer.html");
        } else {
            String requestedPath = websitedir + httpExchange.getRequestURI().getPath();
            // requestedPath =  (requestedPath.startsWith("/")) ? requestedPath.substring(1) : requestedPath;
            inputStream =  MyHttpHandler.class.getResourceAsStream(requestedPath);
        }
        if (inputStream == null) {
            System.out.println("instream null, exit");
            System.exit(123);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder htmlBuilder = new StringBuilder();

        String nextLine = null;
        while ((nextLine = reader.readLine()) != null) {
            htmlBuilder.append(nextLine.replaceAll("\\$name", requestParamValue));
            // System.out.println("readLine: " + nextLine);
        }

        String htmlResponse = htmlBuilder.toString();
        System.out.println("respond to : " + requestParamValue);
        System.out.println(htmlResponse);
        // this line is a must
        httpExchange.sendResponseHeaders(200, htmlResponse.getBytes().length);


        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}

