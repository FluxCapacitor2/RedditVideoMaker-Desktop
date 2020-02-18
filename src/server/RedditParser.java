package server;

import com.google.gson.Gson;
import main.Config;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;

public class RedditParser {

    private static HashMap subreddits = null;

    public static String main(String url, boolean hideButtons) throws IOException {

        boolean autoCapture = url.contains("autoCapture");

        if (subreddits == null) {
            subreddits = new Gson().fromJson(new InputStreamReader(new FileInputStream(new File(Config.getLibraryFolder(), "subreddits.json"))), HashMap.class);
        }

        //Open Reddit and parse its HTML
        String redditUrl = "https://old.reddit.com/" + url;
        System.out.println("Waiting for " + redditUrl);
        Document doc;
        try {
            doc = Jsoup.connect(redditUrl).get();
        } catch (UnknownHostException e) {
            return "<h1>Couldn't reach Reddit</h1><script>window.location.reload();</script>";
        }
        System.out.println("Done! Adding RVM components...");
        //Create "Add" and "Remove" buttons
        Elements els = doc.select(".usertext-body");

        String postTitle = doc.select("a.title").text();

        doc.select("div.side").remove();
        doc.select("head").get(0).prepend("<link type='text/css' rel='stylesheet' href='/res_nightmode.css' />");
        doc.select("title").get(0).text("RVM | " + doc.select("title").text());
        doc.select("body").addClass("nightmode");
        doc.select("p.parent").remove();
        doc.select("div#sr-header-area").remove();
        try {
            doc.selectFirst("link[title=applied_subreddit_stylesheet]").remove();
        } catch (Exception ignored) {
        }

        if (url.contains("/comments/")) {
            if (!hideButtons) {
                doc.select(".usertext-body .md").attr("contenteditable", "true");
                doc.selectFirst(".top-matter").attr("contenteditable", true);
            }
            doc.select("#header")
                    //Post info (meant for posts only)
                    .append(
                            "<form id='postOptions'>\n" +
                                    "    <p>Post Type:</p>\n" +
                                    "    <div class='fw-on-mobile'><input type='radio' name='postType' value='none' id='pt1'><label for='pt1'>None</label></div>\n" +
                                    "    <div class='fw-on-mobile'><input type='radio' name='postType' value='first' id='pt2'><label for='pt2'>First</label></div>\n" +
                                    "        <div class='fw-on-mobile'><input type='radio' name='postType' value='last' id='pt3'><label for='pt3'>Last</label></div>\n" +
                                    "            <div class='fw-on-mobile'><input type='radio' name='postType' value='firstandlast' id='pt4'><label for='pt4'>First & Last</label></div>\n" +
                                    "                <div class='fw-on-mobile'><input type='text' name='thumbnailText' placeholder='Highlight text " +
                                    "using < and >.'\n" +
                                    "                                        style='width:300px;' value='Loading...' id='tText'><br class='show-on-mobile'></div>\n" +
                                    "    <div class='fw-on-mobile'><input type='checkbox' name='isFeatured' id='isFeatured'><label for='isFeatured'>Feature this\n" +
                                    "    post in the YouTube video title.</label><br class='show-on-mobile'></div>\n" +
                                    "</form>\n" +
                                    "<button id='sendBtn'>Send</button>");
        } else {
            //If there is no comments on the page, then this is a list of posts.
            for (Element topMatter : doc.select("div.top-matter")) {
                if (topMatter.select("span.domain").text().contains("AskReddit")) {
                    //If it's a post on r/AskReddit, offer an instant capture button
                    String href = topMatter.selectFirst("a.title").attr("href");
                    topMatter.prepend("<a href='" + href + "?capture=true&autoCapture=true&hideButtons=true&limit=500' target='_blank'><button>Instant Capture</button></a>");
                }
            }
        }
        doc.append("<script type='text/javascript'>document.getElementById('tText').value = decodeURIComponent(\"" + URLEncoder.encode(postTitle, "UTF-8") + "\").replace(/\\+/g, ' ');</script>");

        if (autoCapture && hideButtons) {
            //If the link specified to auto capture, hide all second-level child comments when we're capturing.
            doc.select(".child .child").remove();
            //Also remove all comments after the second child comment of every top-level comment
            for (Element parent : doc.select(".thing.comment")) {
                if (parent.select(".child").size() != 0) {
                    int i = 0;
                    for (Element child : parent.select(".child>div>.thing")) {
                        if (i >= 1) {
                            System.out.println("Removing #" + i + " child comment.");
                            child.remove();
                        }
                        i++;
                    }
                }
            }
        }

        for (Object key : subreddits.keySet()) {
            doc.select("#header").prepend("<button><a href='/" + key + "'>" + key + "</a></button>");
        }

        System.out.println("Finished removing the sidebar, subreddit stylesheet, and adding the send button.");

        if (!hideButtons) {
            doc.body().addClass("no-hide-buttons");
            for (Element e : els) {
                String thingId = e.siblingElements().select("input[name=thing_id]").val();
                //System.out.println("[comment found] " + e.html());
                e.prepend("<button class='btn addComment' id='rvm_add_" + thingId + "'>Add</button>" +
                        "<button class='btn removeComment' id='rvm_rem_" + thingId + "' style='display:none;'>Remove</button>");
                //e.attr("contenteditable", true);
            }
            System.out.println("Finished creating 'add' and 'remove' buttons for each comment.");
        } else System.out.println("Creating 'add' and 'remove' buttons was disabled via a query parameter.");

        replaceRedditLinks(doc.head());
        replaceRedditLinks(doc.body());

        System.out.println("Finished rerouting all reddit.com links back to our server.");

        //noinspection HtmlUnknownTarget
        doc.append("<script src='/rvm_js.js'></script>");
        if (hideButtons) {
            //noinspection HtmlUnknownTarget
            doc.append("<script src='/dom_to_image.js'></script>");
            String titleThingId = doc.selectFirst(".thing").attr("data-fullname");
            doc.append("<span style='display: none !important;' id='title-thing-id' data-thing-id='" + titleThingId + "'></span>");
        }

        System.out.println("Finished injecting the javascript.");

        //Return this HTML to the web server to be displayed.
        System.out.println("All done! Returning the Reddit HTML to the client.");
        return doc.html();
    }

    private static void replaceRedditLinks(Element el) {
        Elements children = el.children();
        if (children.size() == 0) {
            el.text(replaceRedditLinks(el.text()));
            if (el.hasAttr("href")) {
                el.attr("href", replaceRedditLinks(el.attr("href")));
            }
        } else {
            children.forEach(RedditParser::replaceRedditLinks);
        }
    }

    private static String replaceRedditLinks(String input) {
        String host;
        try {
            host = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            host = "127.0.0.1"; //127.0.0.1 is the same as "localhost"
        }
        return input
                .replace("old.reddit.com", host + ":8080")
                .replace("reddit.com", host + ":8080")
                .replace("www.reddit.com", host + ":8080")
                .replace("https://", "http://");
    }
}
