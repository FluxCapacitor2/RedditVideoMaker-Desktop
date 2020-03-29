package server;

import com.google.gson.Gson;
import main.Config;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedule {

    private static final int COMMENTS_THRESHOLD = 2500;

    public static void main(String[] args) throws IOException {
        Server.main(new String[]{});
        System.out.println("Server started. Now starting schedule...");

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(Schedule::loop, 4, 8, TimeUnit.HOURS);
    }

    private static void loop() {
        //Make a new Reddit video now!
        try {
            System.out.println("Searching for a new post with over " + COMMENTS_THRESHOLD + " comments...");
            //Get the list of videos that we've already created (stored by Reddit's permalink for each post)
            Gson gson = new Gson();
            File file = new File(Config.getLibraryFolder(), "created.json");
            CreatedList list = gson.fromJson(new FileReader(file), CreatedList.class);
            //Look at the list of posts available to us
            Document d = Jsoup.connect("http://localhost:" + Server.port + "/r/AskReddit/top/?sort=top&t=day").get();
            boolean postFound = false;
            int maxComments = 0;
            for (Element e : d.select("div.thing:not(.promoted)")) {
                Element title = e.selectFirst("a.title");
                String url = title.attr("href");
                //Select the top post that's not in the list
                boolean duplicate = false;
                for (String link : list.created) {
                    if (link.equals(url)) {
                        duplicate = true;
                        break;
                    }
                }
                if (duplicate) continue;
                //Skip advertisment posts
                if (url.contains("alb.reddit.com")) continue;
                //Skip posts with less comments than the requirement
                int comments = Integer.parseInt(e.select("a.bylink.comments").text().replace(" comments", "").trim());
                maxComments = Math.max(maxComments, comments);
                if (comments < COMMENTS_THRESHOLD) continue;
                //Skip posts marked NSFW
                if (e.select(".nsfw-stamp").size() > 0) continue;
                postFound = true;
                System.out.println("Found video to create: " + title.text() + " @ " + url);
                System.out.println("Post has " + comments + " comments.");
                //We found one!
                //Add it to the list
                System.out.println("Rewriting list...");
                list.created = Capture.append(list.created, url);
                String json = gson.toJson(list);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(json);
                writer.close();
                //Make a video from it
                Map<String, String> query = new HashMap<>();
                Capture.autoCapture(url + "?limit=500&hideButtons=true&autoCapture=true", query);
                //Done!
                System.out.println("Done!");
                break;
            }
            if (!postFound) {
                System.out.println("A post was not found! (Max number of comments found on the first page: " + maxComments + ")");
            }
            System.out.println("Search finished. A video was " + (postFound ? "" : "not ") + "rendered as a result.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
