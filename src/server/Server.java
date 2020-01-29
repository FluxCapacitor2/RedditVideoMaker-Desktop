package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.Main;
import main.VideoManifest;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {

    static VideoManifest manifest = new VideoManifest();

    public static void main(String[] args) throws IOException {
        System.out.println("Starting...");
        short port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 100);
        //server.createContext("/reddit", new SubredditHandler());
        //server.createContext("/r/", new SubredditHandler());
        //server.createContext("/api/morechildren", new RedditApiMoreChildrenHandler());
        //server.createContext("/res_nightmode.css", new FileHandler(Server.class.getResource("res_nightmode.css"), "text/css"));
        server.createContext("/", new GeneralHandler());
        //Thread control is given to executor service.
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server is running at port " + port);
        System.out.println("http://" + Inet4Address.getLocalHost().getHostAddress() + ":" + port + "/");
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

    private static class GeneralHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                String url = httpExchange.getRequestURI().toString();
                if (!url.contains("/renderstatus.json"))
                    System.out.println(httpExchange.getRequestMethod() + " " + url);
                //System.out.println("[GeneralHandler] finding destination for url: " + url);
                if (url.contains("/r/") || url.contains("/u/") || url.contains("reddit.com")) {
                    //System.out.println("[GeneralHandler] delegating handling to SubredditHandler");
                    new SubredditHandler().handle(httpExchange);
                } else if (url.equals("/")) {
                    //System.out.println("[GeneralHandler] delegating handling to SubredditHandler");
                    new SubredditHandler().handle(httpExchange);
                } else if (url.contains("/res_nightmode.css")) {
                    //System.out.println("[GeneralHandler] delegating handling to FileHandler (res_nightmode.css)");
                    new FileHandler("res_nightmode.css", "text/css").handle(httpExchange);
                } else if (url.contains("/rvm_js.js")) {
                    //System.out.println("[GeneralHandler] delegating handling to FileHandler (rvm_js.js)");
                    new FileHandler("rvm_js.js", "application/javascript").handle(httpExchange);
                } else if (url.contains("/dom_to_image.js")) {
                    //System.out.println("[GeneralHandler] delegating handling to FileHandler (dom_to_image.js)");
                    new FileHandler("dom_to_image.js", "application/javascript").handle(httpExchange);
                } else if (new File(System.getProperty("user.home") + "/IdeaProjects/RedditVideoMaker-Desktop/src/server/webroot", url).exists()) {
                    File f = new File(System.getProperty("user.home") + "/IdeaProjects/RedditVideoMaker-Desktop/src/server/webroot", url);
                    //System.out.println("[GeneralHandler] delegating request for " + url + " to FileHandler");
                    new FileHandler(f.toURI().toURL().toString(), getContentTypeFromExtension(f.getName().split("\\.")[f.getName().split("\\.").length - 1])).handle(httpExchange);
                } else if (url.contains("/status")) {
                    new CaptureStatusHandler().handle(httpExchange);
                } else if (url.contains("/renderstatus.json")) {
                    //System.out.println("[GeneralHandler] delegating handling to RenderStatusHandler");
                    new RenderStatusHandler().handle(httpExchange);
                    //System.out.println("[GeneralHandler] Done!");
                } else {
                    System.out.println("404 Not Found");
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

    private static class CaptureStatusHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream os = httpExchange.getResponseBody();
            String captureStatusResponse = "<html lang='en' style='font-family: Roboto, sans-serif;'>\n" +
                    "<link href='https://fonts.googleapis.com/css?family=Roboto' rel='stylesheet'>\n" +
                    "<div id='working' style='font-family: '>\n" +
                    "    " +
                    "<h2>Working...</h2>\n" +
                    "    " +
                    "<p id='progress'>The progress of the capture should update momentarily.</p>\n" +
                    "    " +
                    "<progress max='1' value='0' id='bar' style='width:100%;'></progress>\n" +
                    "    <p>Log</p>\n" +
                    "    <textarea id='log' style='width:100%;height:80%;'></textarea>\n" +
                    "</div>\n" +
                    "<div id='done' style='display:none;'>\n" +
                    "    <h2>Done!</h2>\n" +
                    "    <p>If this is the last post in the video, your video is finished! If not, you can go back and add more posts.</p>\n" +
                    "</div>\n" +
                    "<script type='text/javascript'>\n" +
                    "    var x = new XMLHttpRequest();\n" +
                    "\n" +
                    "    x.onreadystatechange = function () {\n" +
                    "        if (this.readyState === 4 && this.status === 200) {\n" +
                    "            //Success!\n" +
                    "            var json = JSON.parse(x.responseText);\n" +
                    "            document.getElementById('progress').innerHTML = '<strong>' + json.progress.global.title + '</strong><br>' + Math.round(json.progress.global.progress * 100) + '%, ' + json.progress.global.timeRemaining + ' remaining';\n" +
                    "            document.getElementById('bar').value = json.progress.global.progress;\n" +
                    "            var textarea = document.getElementById('log');\n" +
                    "            textarea.value = json.progress.log;\n" +
                    "            " +
                    "textarea.scrollTop = textarea.scrollHeight;\n" +
                    "            " +
                    "if (json.progress.global.title == 'Done') {\n" +
                    "                document.getElementById('working').style.display = 'none';\n" +
                    "                document.getElementById('done').style.display = 'block';\n" +
                    "                window.location.href = '/" + Capture.getSubreddit() + "';\n" +
                    "            } else {\n" +
                    "                document.getElementById('working').style.display = 'block';\n" +
                    "                document.getElementById('done').style.display = 'none';\n" +
                    "            }\n" +
                    "\n" +
                    "            setTimeout(function () {\n" +
                    "    " +
                    "            x.open('GET', '/renderstatus.json', true);\n" +
                    "                x.send();\n" +
                    "            }, 500);\n" +
                    "        } else if (this.status != 200 && this.status != 0 && this.readyState == 4) {\n" +
                    "            console.log(\"XHR Finished Loading: readyState=\", this.readyState, \", status=\", this.status, \".\");\n" +
                    "            //The process is probably done!\n" +
                    "\n" +
                    "        }\n" +
                    "    };\n" +
                    "    x.open('GET', '/renderstatus.json', true);\n" +
                    "    x.send();\n" +
                    "</script>\n" +
                    "</html>";
            httpExchange.sendResponseHeaders(200, captureStatusResponse.getBytes().length);
            os.write(captureStatusResponse.getBytes());
            os.close();
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
                    String response = "<h3>Redirecting...</h3><script type='text/javascript'>window.location.href='/status';</script>";
                    t.getResponseHeaders().set("Content-Type", "text/html");
                    t.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    System.out.println("Capturing page...");
                    String[] selection = query.get("selection").split(",");

                    String url = "http://localhost:8080" + t.getRequestURI().getPath() + "?limit=500&hideButtons=true";
                    //Execute this in a new thread so we can respond to the request.
                    Capture.main(url, selection, query);
                } else {
                    System.out.println("RedditHandler called normally. Opening Reddit...");
                    //Go to Reddit & create "add" buttons, etc.
                    String response = RedditParser.main(t.getRequestURI().toString(), (query != null && query.containsKey("hideButtons")));
                    Headers headers = t.getResponseHeaders();
                    //headers.add("Content-Type", "text/html; charset=UTF-8");
                    headers.add("Content-Type", "text/html; charset=ISO-8859-1");
                    t.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = t.getResponseBody();
                    try {
                        os.write(response.getBytes());
                        os.close();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
                System.out.println("SubredditHandler finished.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class RenderStatusHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            //System.out.println("[RenderStatusHandler] Gathering progress from Main");
            httpExchange.getResponseHeaders().set("Content-Type", "application/json");

            String response = Main.getStatus();

            Headers headers = httpExchange.getResponseHeaders();
            //Set the charset so the emojis and special characters display correctly.
            headers.add("Content-Type", "text/html; charset=ISO-8859-1");
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static class FileHandler implements HttpHandler {
        private String resource;
        private String contentType;

        FileHandler(String resource, String contentType) {
            this.resource = resource;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            //System.out.println("[FileHandler] Initializing (resource=" + resource.toString() + ",contentType=" + contentType + ")");
            String response;
            BufferedReader bf = new BufferedReader(new InputStreamReader(Server.class.getResourceAsStream(resource)));
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
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
