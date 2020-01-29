package server;

import com.google.gson.Gson;
import main.Config;
import main.Main;
import main.VideoManifest;
import main.VideoManifestComment;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Capture {

    private static final String CHROME_DRIVER = "D:/chromedriver/chromedriver.exe";
    private static long startTime;

    private static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    private static int captured = 0;
    private static int total = 0;
    private static boolean isCapturing = false;

    public static boolean isCapturing() {
        return isCapturing;
    }

    public static double getProgress() {
        return (double) captured / (double) total;
    }

    public static long getStartTime() {
        return startTime;
    }

    public static void main(String url, String[] ids, Map<String, String> query) throws IOException {

        isCapturing = true;
        Main.out("[Capture] Starting...");

        String selectionType = query.get("postType"); //"first", "none", or "last".
        String uid;
        boolean isLast = false;

        //a String representing the text to be shown in this title's thumbnail.
        String thumbnailText = query.get("thumbnailText");
        //If this post is going to be the one featured in the YouTube video title. ("true" or "false")
        String isFeatured = query.get("isFeatured");

        switch (selectionType) {
            case "last":
                Main.out("[Selection Type] This is the LAST post of the video.");
                //Last post of the video! Run `Main` after we take our screenshots.
                isLast = true;
                //We don't `break` here because we can reuse the code in the next case.
            case "none":
                //In the middle of the video
                Main.out("[Selection Type] Gathering current manifest uid...");
                uid = Config.prefs.get("currentManifestUid", "null");
                //DON'T clear the current manifest because this isn't the first post (a new video)
                break;
            case "first":
                //First post of the video, new manifest!
                Main.out("[Selection Type] Creating new manifest uid...");
                uid = String.valueOf(System.currentTimeMillis());
                Main.out("[Capture] Clearing current video manifest because this is the first post!");
                Server.manifest = new VideoManifest();
                Config.prefs.put("currentManifestUid", uid);
                //Because this is the first post, clear the logs
                Main.log = new StringBuilder();
                break;
            case "firstandlast":
                Main.out("[Selection Type] First and last post: Creating new manifest uid...");
                uid = String.valueOf(System.currentTimeMillis());
                Main.out("[Capture] Clearing current video manifest because this is the first post! (and last)");
                Server.manifest = new VideoManifest();
                Config.prefs.put("currentManifestUid", uid);
                isLast = true;
                break;
            default:
                Main.err("Invalid selection type: " + selectionType + ". Accepted values are 'none', 'first', 'last', and 'firstandlast'. Reverting to 'first'...");
                uid = String.valueOf(System.currentTimeMillis());
                break;
        }
        if (uid.equals("null")) {
            //We couldn't find the uid that we should have stored earlier!
            Main.err("There was an error finding the current video UID. Aborting...");
            return;
        }

        Main.out("[Capture] Set manifest UID to " + uid);
        Main.out("[Capture] Starting ChromeDriver...");

        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER);
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.setProxy(null);
        WebDriver driver = new ChromeDriver(options);
        Main.out("[Capture] Driver started");
        driver.get(url);
        Main.out("[Capture] URL Loaded");

        if (Server.manifest.URLs == null) Server.manifest.URLs = new String[]{};
        if (Server.manifest.titles == null) Server.manifest.titles = new String[]{};
        if (Server.manifest.subreddits == null) Server.manifest.subreddits = new String[]{};
        if (Server.manifest.comments == null) Server.manifest.comments = new VideoManifestComment[]{};
        if (Server.manifest.thumbnailTexts == null) Server.manifest.thumbnailTexts = new String[]{};
        if (Server.manifest.isFeatured == null) Server.manifest.isFeatured = new Boolean[]{};

        String sr = "/r/" + driver.findElement(By.cssSelector("span.redditname>a")).getText() + "/";
        Server.manifest.URLs = append(Server.manifest.URLs, driver.findElement(By.cssSelector("link[rel=shorturl]")).getAttribute("href"));
        Server.manifest.titles = append(Server.manifest.titles, driver.findElement(By.cssSelector("a.title")).getText());
        Server.manifest.subreddits = append(Server.manifest.subreddits, sr);
        Server.manifest.thumbnailTexts = append(Server.manifest.thumbnailTexts, thumbnailText);
        Server.manifest.isFeatured = append(Server.manifest.isFeatured, Boolean.parseBoolean(isFeatured));
        ArrayList<VideoManifestComment> comments = new ArrayList<>();

        String titleThingId = driver.findElement(By.id("title-thing-id")).getAttribute("data-thing-id");

        ArrayList<String> thingIds = new ArrayList<>();
        thingIds.add(titleThingId);
        Collections.addAll(thingIds, ids);

        total = 0;
        captured = 0;
        startTime = System.currentTimeMillis();

        //Calculate the total number of posts to track progress.
        for (String t : thingIds) {
            if (total == 0) {
                total++; //Top matter is always just 1 post
            } else {
                total += driver.findElements(By.cssSelector("#thing_" + t + ">.entry .md p")).size();
            }
        }

        int i = 0;
        for (String thingId : thingIds) {
            Main.out("Taking screenshot of post: " + thingId);

            if (query.containsKey("edit_title")) {
                ((JavascriptExecutor) driver).executeScript("document.querySelector('a.title').innerHTML = " + new Gson().toJson(query.get("edit_title")) + ";");
            }

            if (query.containsKey("edit_" + thingId)) {
                //Main.out("URL argument found: edit_" + thingId + " = " + query.get("edit_" + thingId));
                String val = query.get("edit_" + thingId);
                //Change the text to the edited value
                ((JavascriptExecutor) driver).executeScript("document.querySelector('#thing_" + thingId + ">.entry .md').innerHTML = " + new Gson().toJson(val) + ";");
            } else {
                Main.out("No 'edit' argument found for " + thingId);
            }

            List<WebElement> els2 = new ArrayList<>();
            String paraCssSelector;
            try {
                List<WebElement> els;
                if (i == 0) {
                    Main.out("Capturing top matter (post title).");
                    els = driver.findElements(By.className("top-matter"));
                    paraCssSelector = ".top-matter";
                } else {
                    els = driver.findElements(By.cssSelector("#thing_" + thingId + ">.entry .md p"));
                    paraCssSelector = "#thing_" + thingId + ">.entry .md p";
                }
                for (WebElement e : els) {
                    if (!e.getAttribute("class").contains("tagline")) {
                        els2.add(e);
                    }
                }
            } catch (NoSuchElementException e) {
                i++;
                Main.out("Skipping post \"" + thingId + "\" because it could not be found.");
                continue;
            }

            int j = 0;
            for (WebElement p : els2) {
                captured++;
                Main.out("Capturing " + paraCssSelector + "[" + j + "].");
                try {
                    if (p.getAttribute("class").contains("tagline")) {
                        j++;
                        continue;
                    }
                    VideoManifestComment c = new VideoManifestComment();
                    c.text = (i == 0 ? p.findElement(By.cssSelector("a.title")).getText() : p.getText());

                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String imageDataURL = (String) js.executeAsyncScript("var thingId = arguments[0];\n" +
                            "var callback = arguments[arguments.length - 1];\n" +
                            "console.log(\"Capturing item with thing ID \", thingId);\n" +
                            "var thing = document.getElementById(\"thing_\" + thingId);\n" +
                            "var el = thing.querySelectorAll('" + paraCssSelector + "')[" + (j) + "];\n" +
                            "console.log('Capturing element', el);" +
                            "thing.scrollIntoView(true);\n" +
                            "domtoimage.toPng(el, {\n" +
                            "    bgcolor: '#414141'\n" +
                            "}).then(function(dataURL) {\n" +
                            "    console.log(\"Finished capturing image\", dataURL);\n" +
                            "    callback(dataURL);\n" +
                            "}).catch(function(error) {\n" +
                            "    console.error(\"Error rendering image\", error);\n" +
                            "});", thingId);

                    //Main.out("Image data gathered: " + imageDataURL);
                    byte[] data;
                    try {
                        data = DatatypeConverter.parseBase64Binary(imageDataURL.split(",")[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        j++;
                        Main.out("Skipping post because there was no image data.");
                        continue;
                    }
                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
                    File screenshotLocation = new File(Config.getDownloadsFolder() + "/rvm_dl_" + uid + "_thing_" + thingId + "_" + j + "_para" + (i == 0 ? "_title" : "") + ".png");
                    ImageIO.write(bufferedImage, "png", screenshotLocation);

                    c.name = screenshotLocation.getName();
                    if (i == 0) c.isTitle = true;
                    c.thingId = thingId + "_" + j + "_para" + (i == 0 ? "_title" : "");
                    c.DLid = uid;
                    c.subreddit = sr;
                    c.indexInPost = i;

                    comments.add(c);
                    j++;
                } catch (NoSuchElementException e) {
                    Main.out("Skipping capture because the element to capture could not be found. (" + paraCssSelector + ")");
                }
            }
            i++;
        }
        driver.quit();
        for (VideoManifestComment c : comments) {
            Server.manifest.comments = append(Server.manifest.comments, c);
        }
        //Write the manifest file
        Gson gson = new Gson();
        String json = gson.toJson(Server.manifest);
        File file = new File(Config.getDownloadsFolder() + "/rvm_manifest_" + uid + ".json");
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        FileOutputStream f = new FileOutputStream(file);
        f.write(json.getBytes());
        f.close();
        Main.out("[Capture] isLast = " + isLast);
        isCapturing = false;
        Main.out("[Capture] Finished.");
        if (isLast) {
            Config.prefs.put("currentManifestUid", String.valueOf(System.currentTimeMillis() + 1000));
            Server.manifest = new VideoManifest();
            try {
                //Start rendering the video because the capture finished.
                isCapturing = false;
                Main.main(new String[]{});
            } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSubreddit() {
        try {
            return Server.manifest.subreddits[0];
        } catch (NullPointerException e) {
            return "status";
        }
    }
}
