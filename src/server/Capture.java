package server;

import com.google.gson.Gson;
import main.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    private static final int SOFT_CHAR_LIMIT = 600;
    private static long startTime;

    static <T> T[] append(T[] arr, T element) {
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

    public static void autoCapture(String url, Map<String, String> query) throws IOException {
        url += "&autoCapture=true";
        Main.out("[Capture] AutoCapture called for \"" + url + "\".");
        Document d = Jsoup.parse(RedditParser.main(url, true));
        ArrayList<String> comments = new ArrayList<>();
        int charCount = 0;
        for (Element e : d.select(".thing")) {
            String thingId = e.attr("data-fullname");
            if (thingId.length() == 10) {
                comments.add(thingId);
                if (e.select(".entry .md").size() > 0) {
                    charCount += e.selectFirst(".entry .md").text().length();
                } else charCount += 500;
            }
            if (comments.size() >= 300 || charCount > 25000) break;
        }
        //Set values that are expected by SelectiveCapture
        query.put("postType", "firstandlast");
        query.put("thumbnailText", d.selectFirst("a.title").text());
        query.put("isFeatured", "true");
        Main.out("Sending auto-gathered selection of " + comments.size() + " comments to SelectiveCapture.");
        main("localhost:" + Server.port + url, comments.toArray(new String[]{}), query);
    }

    public static void main(String url, String[] ids, Map<String, String> query) throws IOException {
        Main.out("[Capture] Selective capture called for \"" + url + "\".");

        isCapturing = true;
        Main.out("[Capture] Starting...");

        String selectionType = query.get("postType"); //"first", "none", "last", or "firstandlast".
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
        options.setHeadless(false);
        options.setProxy(null);
        WebDriver driver = new ChromeDriver(options);
        Main.out("[Capture] Driver started");
        driver.get(url);
        Main.out("[Capture] URL Loaded: " + url);

        if (Server.manifest.URLs == null) Server.manifest.URLs = new String[]{};
        if (Server.manifest.titles == null) Server.manifest.titles = new String[]{};
        if (Server.manifest.subreddits == null) Server.manifest.subreddits = new String[]{};
        if (Server.manifest.comments == null) Server.manifest.comments = new VideoManifestComment[]{};
        if (Server.manifest.thumbnailTexts == null) Server.manifest.thumbnailTexts = new String[]{};
        if (Server.manifest.isFeatured == null) Server.manifest.isFeatured = new Boolean[]{};

        //System.out.println(driver.getPageSource());

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
                    if (driver.findElement(By.cssSelector("#thing_" + thingId + ">.entry .md")).getText().length() > SOFT_CHAR_LIMIT) {
                        //Divide into groups of paragraphs if the post is over 4000 characters
                        els = driver.findElements(By.cssSelector("#thing_" + thingId + ">.entry .md p"));
                        int currentGroupLength = 0;
                        int index = 0;
                        ArrayList<Integer> indexes = new ArrayList<>();
                        for (WebElement el : els) {
                            currentGroupLength += el.getText().length();
                            index++;
                            if (currentGroupLength > SOFT_CHAR_LIMIT) {
                                indexes.add(index);
                                index = 0;
                            }
                        }
                        int groupNumber = 0;
                        System.out.println("Groups (by # of paragraphs in each): " + indexes.toString());
                        for (int len : indexes) {
                            String js =
                                    "var newDiv = document.createElement('div');\n" +
                                            "newDiv.setAttribute('id', 'thing_" + thingId + "_group_" + groupNumber + "');\n" +
                                            "newDiv.setAttribute('class', 'thingGroup');\n" +
                                            "document.querySelector('#thing_" + thingId + ">.entry .md').append(newDiv);\n";

                            for (int temp = len; temp > 0; temp--) {
                                //noinspection StringConcatenationInLoop
                                js += "newDiv.appendChild(document.querySelector('#thing_" + thingId + ">.entry .md p:nth-child(1)'));\n";
                            }

                            System.out.println("---Executing JS---\n" + js + "\n------------------");

                            ((JavascriptExecutor) driver).executeScript(js);

                            groupNumber++;
                        }
                        System.out.println("Grouped.");
                        paraCssSelector = "#thing_" + thingId + " .thingGroup";
                        els = driver.findElements(By.cssSelector(paraCssSelector));
                    } else {
                        //Don't divide into paragraphs if the post is less than or equal to 4000 characters
                        els = driver.findElements(By.cssSelector("#thing_" + thingId + ">.entry"));
                        paraCssSelector = "#thing_" + thingId + ">.entry";
                    }
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
                    if (i == 0) {
                        c.text = p.findElement(By.cssSelector("a.title")).getText();
                    } else {
                        c.text = p.getText();
                        try {
                            c.text = p.findElement(By.cssSelector(".md")).getText();
                            System.out.println("Comment text was assigned from child <p> text, not from parent element.");
                        } catch (NoSuchElementException ignored) {

                        }
                    }

                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String imageDataURL = (String) js.executeAsyncScript("var thingId = arguments[0];\n" +
                            "var callback = arguments[arguments.length - 1];\n" +
                            "console.log(\"Capturing item with thing ID \", thingId);\n" +
                            "var thing = document.getElementById(\"thing_\" + thingId);\n" +
                            "var el = thing.querySelectorAll('" + paraCssSelector + "')[" + j + "];\n" +
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

                    try {
                        driver.findElement(By.cssSelector(paraCssSelector + " .tagline"));
                        System.out.println("Post already has a tagline.");
                    } catch (NoSuchElementException ignored) {
                        System.out.println("Post does not have a tagline. Appending it to the image...");
                        if (i != 0) {
                            String taglineDataURL = (String) js.executeAsyncScript("var thingId = arguments[0];\n" +
                                    "var callback = arguments[arguments.length - 1];\n" +
                                    "console.log(\"Capturing tagline with thing ID \", thingId);\n" +
                                    "var thing = document.getElementById(\"thing_\" + thingId);\n" +
                                    "var el = thing.querySelector('p.tagline');\n" +
                                    "console.log('Capturing element', el);thing.scrollIntoView(true);\n" +
                                    "domtoimage.toPng(el, {\n" +
                                    "    bgcolor: '#414141'\n" +
                                    "}).then(function(dataURL) {\n" +
                                    "    console.log(\"Finished capturing image\", dataURL);\n" +
                                    "    callback(dataURL);\n" +
                                    "}).catch(function(error) {\n" +
                                    "    console.error(\"Error rendering image\", error);\n" +
                                    "    callback(null);\n" +
                                    "});", thingId);

                            byte[] taglineData = new byte[]{};
                            try {
                                taglineData = DatatypeConverter.parseBase64Binary(taglineDataURL.split(",")[1]);
                            } catch (ArrayIndexOutOfBoundsException ignored2) {
                            }

                            BufferedImage tagline = ImageIO.read(new ByteArrayInputStream(taglineData));
                            File taglineLocation = new File(Config.getDownloadsFolder(), "tagline.png");
                            ImageIO.write(tagline, "png", taglineLocation);

                            AppendTagline.appendTagline(screenshotLocation, taglineLocation, screenshotLocation);

                            //noinspection ResultOfMethodCallIgnored
                            taglineLocation.delete();
                        }
                    }

                    c.name = screenshotLocation.getName();
                    c.isTitle = i == 0;
                    c.thingId = thingId + "_" + j + "_para" + (i == 0 ? "_title" : "");
                    c.DLid = uid;
                    c.subreddit = sr;
                    c.indexInPost = i;
                    //We're going to check if it is a parent comment by its CSS `orphans` value
                    // (doesn't change how anything looks in this case, but for child comments I set it to '10')
                    boolean isParentComment = !p.getCssValue("orphans").equals("10");
                    c.isParent = (
                            //Is a parent comment (is not surrounded by anything that matches ".child" css selector)
                            isParentComment
                                    //Is the first paragraph in the comment
                                    && j == 0
                                    //Isn't the first comment in the vid (the title)
                                    && i != 0);

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
        } else {
            Main.setTitle("Done");
        }
    }

    public static String getSubreddit() {
        try {
            return Server.manifest.subreddits[0];
        } catch (NullPointerException e) {
            return null;
        }
    }
}
