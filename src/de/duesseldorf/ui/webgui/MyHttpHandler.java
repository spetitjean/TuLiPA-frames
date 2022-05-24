package de.duesseldorf.ui.webgui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.duesseldorf.rrg.io.RRGXMLBuilder;
import org.w3c.dom.Document;

import javax.xml.transform.stream.StreamResult;
import java.io.*;
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
        contentTypes.put("xml", "text/xml");
    }

    private String sentence;
    private RRGXMLBuilder rrgxmlBuilder;

    public MyHttpHandler(String sentence, RRGXMLBuilder rrgxmlBuilder) {
        this.sentence = sentence;
        this.rrgxmlBuilder = rrgxmlBuilder;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("handle request: " + httpExchange.getRequestURI());

        // shutdown command
        if (httpExchange.getRequestURI().getPath().contains("shutdown-parser")){
            System.out.println("told to shut down by the GUI window, exiting now");
            System.exit(130);
        }

        handleResponse(httpExchange);
    }

    /**
     * the most important method: ig processes the different get requests for code
     * of the website, or the GET request for th4 parse result
     *
     * @param httpExchange
     * @throws IOException
     */
    private void handleResponse(HttpExchange httpExchange) throws IOException {

        //// Find the resource
        String requestedPath = "";
        if (httpExchange.getRequestURI().getPath().equals("/")) {
            requestedPath = websitedir + "/tulipa-viewer.html";
        } else {
            requestedPath = websitedir + httpExchange.getRequestURI().getPath();
            // requestedPath =  (requestedPath.startsWith("/")) ? requestedPath.substring(1) : requestedPath;
        }

        //// ContentType
        String contentType = contentTypes.getOrDefault(requestedPath.substring(requestedPath.lastIndexOf(".") + 1), "");
        httpExchange.getResponseHeaders().set("Content-Type", contentType);

        byte[] response = null;
        //// Request for the parse Result
        if (requestedPath.contains("PARSEROUTPUT.xml")) {
            Document parseResultDoc = rrgxmlBuilder.build();
            StreamResult streamResult = new StreamResult(new StringWriter());
            rrgxmlBuilder.write(streamResult);
            response = streamResult.getWriter().toString().getBytes();
        } else {

            InputStream inputStream = MyHttpHandler.class.getResourceAsStream(requestedPath);
            if (inputStream == null) {
                System.out.println("instream null, cannot handle request for " + httpExchange.getRequestURI());
            }


            // handle text separately, so that for example we can replace things in html
            if (contentType.startsWith("text")) {
                //System.out.println("process text file " + requestedPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder htmlBuilder = new StringBuilder();

                String nextLine = null;
                while ((nextLine = reader.readLine()) != null) {
                    nextLine = nextLine.replaceAll("\\$sentence", "'" + sentence+"'");
                    htmlBuilder.append(nextLine + "\n");
                    // System.out.println("readLine: " + nextLine);
                }
                String textResponse = htmlBuilder.toString();
                response = textResponse.getBytes();
            } else {
                // process non-text files, like binarys (e.g. font files)
                // System.out.println("process non-text file" + requestedPath);
                response = inputStream.readAllBytes();
            }
        }


        //// send
        httpExchange.sendResponseHeaders(200, response.length);

        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
    }

}

