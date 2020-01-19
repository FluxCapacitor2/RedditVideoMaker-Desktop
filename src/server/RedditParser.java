package server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class RedditParser {
    public static String main(String url, boolean hideButtons) throws IOException {
        //Open Reddit and parse its HTML
        String redditUrl = "https://old.reddit.com/" + url;
        System.out.println("Waiting for " + redditUrl);
        Document doc;
        try {
            doc = Jsoup.connect(redditUrl).get();
        } catch (UnknownHostException e) {
            return "<h1>Couldn't reach Reddit</h1>";
        }
        System.out.println("Done! Adding RVM components...");
        //Create "Add" and "Remove" buttons (VERY similar to the Chrome extension, this is on purpose)
        Elements els = doc.select(".usertext-body");

        String postTitle = doc.select("a.title").text();

        doc.select("div.side").remove();
        doc.select("head").get(0).prepend("<link type='text/css' rel='stylesheet' href='/res_nightmode.css' />");
        doc.select("title").get(0).text("RVM | " + doc.select("title").text());
        doc.select("body").addClass("nightmode");
        doc.select("p.parent").remove();
        //doc.selectFirst("link[title=applied_subreddit_stylesheet]").remove();

        if (url.contains("/comments/")) {
            if (!hideButtons) {
                doc.select(".usertext-body .md").attr("contenteditable", "true");
                doc.selectFirst(".top-matter").attr("contenteditable", true);
            }
            doc.select("body")
                    //Post info (meant for posts only)
                    .prepend("<button id='sendBtn'>Send</button>\n" +
                            "<form id='postOptions'>\n" +
                            "    <input type='radio' name='postType' value='none'>None\n" +
                            "    <input type='radio' name='postType' value='first'>First\n" +
                            "    <input type='radio' name='postType' value='last'>Last<br>\n" +
                            "    <input type='radio' name='postType' value='firstandlast'>First & Last<br>\n" +
                            "    <input type='text' name='thumbnailText' placeholder='Thumbnail text. Highlight text using < and >.' style='width:300px;' value='" + postTitle + "'><br>\n" +
                            "    <input type='checkbox' name='isFeatured' id='isFeatured'><label for='isFeatured'>Feature this post in the YouTube video title.</label><br>\n" +
                            "</form>");
        }
        doc.select("body")
                //Subreddit quick links
                .prepend("<button><a href='/r/ProRevenge'>r/ProRevenge</a></button>\n" +
                        "<button><a href='/r/PettyRevenge'>r/PettyRevenge</a></button>\n" +
                        "<button><a href='/r/MaliciousCompliance'>r/MaliciousCompliance</a></button>\n" +
                        "<button><a href='/r/NuclearRevenge'>r/NuclearRevenge</a></button>\n" +
                        "<button><a href='/r/AskReddit'>r/AskReddit</a></button>\n" +
                        "<button><a href='/r/tifu'>r/tifu</a></button>\n" +
                        "<button><a href='/r/AmITheAsshole'>r/AITA</a></button>\n" +
                        "<button><a href='/r/TalesFromTechSupport'>r/TalesFromTechSupport</a></button>\n" +
                        "<button><a href='/r/TalesFromRetail'>r/TalesFromRetail</a></button><br><br>");

        System.out.println("Finished removing the sidebar, subreddit stylesheet, and adding the send button.");

        if (!hideButtons) {
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
