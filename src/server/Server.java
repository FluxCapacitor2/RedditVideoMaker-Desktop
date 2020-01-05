package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.VideoManifest;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {

    static VideoManifest manifest = new VideoManifest();

    public static void main(String[] args) throws IOException {
        System.out.println("Starting...");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 100);
        //server.createContext("/reddit", new SubredditHandler());
        //server.createContext("/r/", new SubredditHandler());
        //server.createContext("/api/morechildren", new RedditApiMoreChildrenHandler());
        //server.createContext("/res_nightmode.css", new FileHandler(Server.class.getResource("res_nightmode.css"), "text/css"));
        server.createContext("/", new GeneralHandler());
        //Thread control is given to executor service.
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server is running at port 8080");
    }

    private static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    /*
        private static class RedditApiMoreChildrenHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange t) throws IOException {
                String method = t.getRequestMethod();
                if (method.equalsIgnoreCase("POST")) {
                    String url = "https://api.reddit.com/" + t.getRequestURI().toString() + "?";
                    System.out.println("Requesting URL: " + url);
                    Request r = Request.Post(url);
                    t.getRequestHeaders().forEach((name, values) -> r.addHeader(name, values.toString()));
                    r.bodyStream(t.getRequestBody());
                    Response res = r.execute();
                    byte[] response = res.returnContent().asBytes();
                    t.sendResponseHeaders(200, response.length);
                    printRequestInfo(t);
                    OutputStream os = t.getResponseBody();
                    os.write(response);
                    os.close();
                }

            }
        }
    */

    private static void printRequestInfo(HttpExchange exchange) {
        /*
        System.out.println("-- headers --");
        Headers requestHeaders = exchange.getRequestHeaders();
        requestHeaders.entrySet().forEach(System.out::println);

        System.out.println("-- principle --");
        HttpPrincipal principal = exchange.getPrincipal();
        System.out.println(principal);

        System.out.println("-- HTTP method --");
        String requestMethod = exchange.getRequestMethod();
        System.out.println(requestMethod);

        System.out.println("-- query --");
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        System.out.println(query);
         */
    }

    private static class GeneralHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String url = httpExchange.getRequestURI().toString();
                System.out.println("[GeneralHandler] finding destination for url: " + url);
                if (url.contains("/r/") || url.contains("/u/") || url.contains("reddit.com")) {
                    System.out.println("[GeneralHandler] delegating handling to SubredditHandler");
                    new SubredditHandler().handle(httpExchange);
                } else if (url.equals("/")) {
                    System.out.println("[GeneralHandler] delegating handling to SubredditHandler");
                    new SubredditHandler().handle(httpExchange);
                } else if (url.contains("/res_nightmode.css")) {
                    System.out.println("[GeneralHandler] delegating handling to FileHandler (res_nightmode.css)");
                    new FileHandler(Server.class.getResource("res_nightmode.css"), "text/css").handle(httpExchange);
                } else if (url.contains("/rvm_js.js")) {
                    System.out.println("[GeneralHandler] delegating handling to FileHandler (rvm_js.js)");
                    new FileHandler(Server.class.getResource("rvm_js.js"), "application/javascript").handle(httpExchange);
                } else if (url.contains("/dom_to_image.js")) {
                    System.out.println("[GeneralHandler] delegating handling to FileHandler (dom_to_image.js)");
                    new FileHandler(Server.class.getResource("dom_to_image.js"), "application/javascript").handle(httpExchange);
                } else if (url.contains("/capture.js")) {
                    System.out.println("[GeneralHandler] delegating handling to FileHandler (capture.js)");
                    new FileHandler(Server.class.getResource("capture.js"), "application/javascript").handle(httpExchange);
                } else if (new File("C:/Users/tntaw/IdeaProjects/RedditVideoMaker-Desktop/src/server/webroot", url).exists()) {
                    File f = new File("C:/Users/tntaw/IdeaProjects/RedditVideoMaker-Desktop/src/server/webroot", url);
                    System.out.println("[GeneralHandler] delegating request for " + url + " to FileHandler");
                    new FileHandler(f.toURI().toURL(), getContentTypeFromExtension(f.getName().split("\\.")[f.getName().split("\\.").length - 1])).handle(httpExchange);
                } else {
                    httpExchange.sendResponseHeaders(404, 0);
                    OutputStream os = httpExchange.getResponseBody();
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                httpExchange.sendResponseHeaders(500, 0);
                OutputStream os = httpExchange.getResponseBody();
                os.close();
            }
        }

        private String getContentTypeFromExtension(String s) {
            if (s.contains("png")) return "image/png";
            else if (s.contains("jpg")) return "image/jpeg";

            return "text/plain";
        }
    }

    private static class SubredditHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) {
            try {
                //Check if we're supposed to capture...
                System.out.println("SubredditHandler opened!");
                URI uri = t.getRequestURI();
                Map<String, String> query = null;
                if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                    URL reqURL = new URL("http", "localhost", 8080, uri.toString());
                    query = splitQuery(reqURL);
                }
                if (query != null && query.containsKey("capture") && query.get("capture").equals("true")) {
                    String response = "<html lang='en'>\n" +
                            "<h1>Success!</h1>\n" +
                            "<p>The server is now taking the screenshots and rendering the video. Refresh this page periodically for progress\n" +
                            "    updates.</p>\n" +
                            "</html>";
                    t.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    System.out.println("Capturing page...");
                    String[] selection = query.get("selection").split(",");

                    String url = "http://localhost:8080" + t.getRequestURI().getPath() + "?limit=500&hideButtons=true";
                    //Execute this in a new thread so we can respond to the request.
                    Map<String, String> finalQuery = query;
                    new Thread(() -> {
                        try {
                            Capture.main(url, selection, finalQuery);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                } else {
                    System.out.println("RedditHandler called normally. Opening Reddit...");
                    //Go to Reddit & create "add" buttons, etc.
                    String response = RedditParser.main(t.getRequestURI().toString(), (query != null && query.containsKey("hideButtons")));
                    Headers headers = t.getResponseHeaders();
                    headers.add("Content-Type", "text/html");
                    t.sendResponseHeaders(200, response.getBytes().length);
                    printRequestInfo(t);
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
                System.out.println("SubredditHandler finished.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class FileHandler implements HttpHandler {
        private URL resource;
        private String contentType;

        FileHandler(URL resource, String contentType) {
            this.resource = resource;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            System.out.println("[FileHandler] Initializing (resource=" + resource.toString() + ",contentType=" + contentType + ")");
            String response;
            BufferedReader bf = new BufferedReader(new FileReader(new File(resource.getPath())));
            String line;
            StringBuilder responseBuilder = new StringBuilder();
            while ((line = bf.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            bf.close();
            System.out.println("File read finished.");
            response = responseBuilder.toString();
            t.getResponseHeaders().set("Content-Type", this.contentType);
            t.sendResponseHeaders(200, response.getBytes().length);
            printRequestInfo(t);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
