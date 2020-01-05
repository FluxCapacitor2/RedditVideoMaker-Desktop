package server;

import com.google.gson.Gson;
import main.Config;
import main.Main;
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

    private static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

    public static void main(String url, String[] ids, Map<String, String> query) throws IOException {

        String selectionType = query.get("postType"); //"first", "none", or "last".
        String uid;
        boolean isLast = false;

        //a String representing the text to be shown in this title's thumbnail.
        String thumbnailText = query.get("thumbnailText");
        //If this post is going to be the one featured in the YouTube video title. ("true" or "false")
        String isFeatured = query.get("isFeatured");

        switch (selectionType) {
            case "last":
                System.out.println("This is the LAST post of the video.");
                //Last post of the video! Run `Main` after we take our screenshots.
                isLast = true;
                //We don't `break` here because we can reuse the code in the next case.
            case "none":
                //In the middle of the video
                uid = Config.prefs.get("currentManifestUid", "null");
                break;
            case "first":
                //First post of the video, new manifest!
                uid = String.valueOf(System.currentTimeMillis());
                Config.prefs.put("currentManifestUid", uid);
                break;
            default:
                System.err.println("Invalid selection type: " + selectionType + ". Accepted values are 'none', 'first', and 'last'. Reverting to 'first'...");
                uid = String.valueOf(System.currentTimeMillis());
                break;
        }
        if (uid.equals("null")) {
            //We couldn't find the uid that we should have stored earlier!
            System.err.println("There was an error finding the current video UID. Aborting...");
            return;
        }

        System.out.println("Set manifest UID to " + uid);

        System.setProperty("webdriver.chrome.driver", "D:/chromedriver/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.setProxy(null);
        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

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

        int i = 0;
        for (String thingId : thingIds) {
            System.out.println("Taking screenshot of post: " + thingId);
            List<WebElement> els2 = new ArrayList<>();
            String paraCssSelector = "";
            try {
                List<WebElement> els;
                if (i == 0) {
                    System.out.println("Capturing top matter (post title).");
                    els = driver.findElements(By.className("top-matter"));
                    paraCssSelector = ".top-matter";
                } else {
                    //Check if there's an expando
                    /*
                    try {
                        els = el.findElements(By.cssSelector(".expando .md p"));
                        System.out.println("Capturing all paragraphs in expando.");
                        //There is an expando because there was no error thrown above!
                    } catch (NoSuchElementException e) {
                        System.out.println("Capturing all paragraphs normally (no expando).");
                        //Nope, no expando! Just capture normally.
                        els = el.findElements(By.tagName("p"));
                    }
                     */
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
                System.out.println("Skipping post because it could not be found.");
                continue;
            }

            int j = 0;
            for (WebElement p : els2) {
                System.out.println("Capturing " + paraCssSelector + "[" + j + "].");
                if (p.getAttribute("class").contains("tagline")/* || (i != 0 && j == 0)*/) {
                    System.out.println("Skipping screenshot of " + p.getText() + " because it is a tagline.");
                    j++;
                    continue;
                }
                VideoManifestComment c = new VideoManifestComment();
                c.text = (i == 0 ? p.findElement(By.cssSelector("a.title")).getText() : p.getText());

                JavascriptExecutor js = (JavascriptExecutor) driver;
                //js.executeScript("window.scrollTo(" + el.getLocation().getY() + "," + el.getLocation().getX() + ");");
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

                System.out.println("Image data gathered: " + imageDataURL);
                byte[] data;
                try {
                    data = DatatypeConverter.parseBase64Binary(imageDataURL.split(",")[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    j++;
                    System.out.println("Skipping post because there was no image data.");
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
            }
            i++;
        }
        driver.quit();
        for (VideoManifestComment c : comments) {
            Server.manifest.comments = append(Server.manifest.comments, c);
        }
        Gson gson = new Gson();
        String json = gson.toJson(Server.manifest);
        File file = new File(Config.getDownloadsFolder() + "/rvm_manifest_" + uid + ".json");
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        FileOutputStream f = new FileOutputStream(file);
        f.write(json.getBytes());
        f.close();
        if (isLast) {
            Config.prefs.remove("currentManifestUid");
            new Thread(() -> {
                try {
                    Main.main(new String[]{});
                } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        System.out.println("[Capture] Finished.");
    }
}
