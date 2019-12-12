import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import javax.swing.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class UploadVideo {
    private static final Collection<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @return The ID of the newly-uploaded video.
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    static String main(String filePath, String videoTitle, String videoDescription, List<String> videoTags)
            throws GeneralSecurityException, IOException {
        Main.out("Uploading video:\npath=" + filePath + "\ntitle=" + videoTitle + "\ndescription=" + videoDescription +
                "\ntags=" + videoTags);
        YouTube youtubeService = ApiUtils.getService(JSON_FACTORY, SCOPES);

        // Define the Video object, which will be uploaded as the request body.

        Video video = new Video();

        VideoStatus status = new VideoStatus();
        VideoSnippet snippet = new VideoSnippet();

        status.setPrivacyStatus("private");

        //Make sure the title/description doesn't contain demonetized words.
        File f = new File(Config.getLibraryFolder() + "/demonetized_words.txt");
        if (f.exists()) {
            BufferedReader bf = new BufferedReader(new FileReader(f));
            String words = bf.readLine();
            String[] arr = words.split(", ");

            for (String word : arr) {
                if (videoTitle.contains(word)) {
                    JOptionPane.showMessageDialog(null, "Demonetized word found in video title: \"" + word + "\"",
                            "Demonetized Word Found", JOptionPane.WARNING_MESSAGE);
                }
                if (videoDescription.contains(word)) {
                    JOptionPane.showMessageDialog(null, "Demonetized word found in video description: \"" + word + "\"",
                            "Demonetized Word Found", JOptionPane.WARNING_MESSAGE);
                }
                StringBuilder tags = new StringBuilder();
                for (String tag : videoTags) {
                    tags.append(tag);
                }
                if (tags.toString().contains(word)) {
                    JOptionPane.showMessageDialog(null, "Demonetized word found in video tags: \"" + word + "\"",
                            "Demonetized Word Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            Main.err("WARNING: No demonetized words list was found. Video metadata can not be checked for demonetized words.");
        }

        snippet.setTitle(videoTitle);
        snippet.setDescription(videoDescription);
        snippet.setTags(videoTags);
        snippet.setCategoryId("24");

        video.setStatus(status);
        video.setSnippet(snippet);

        File mediaFile = new File(filePath);
        InputStreamContent mediaContent =
                new InputStreamContent("video/*",
                        new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        // Define and execute the API request
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert("snippet,status", video, mediaContent);

        MediaHttpUploader uploader = request.getMediaHttpUploader();

        long startTime = System.currentTimeMillis();

        MediaHttpUploaderProgressListener listener = progress -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    Main.out("Video upload progress changed to " + (progress.getProgress() * 100) + "%");
                    Main.gui.progressLabel.setText(Math.round(progress.getProgress() * 100) + "%");
                    Main.calculateRemainingTime(startTime, 100, (long) (progress.getProgress() * 100));
                    Main.gui.progressBar.setValue((int) (progress.getProgress() * 100));
                    Main.guiFrame.setAlwaysOnTop(true);
                    Main.guiFrame.setAlwaysOnTop(Config.getAlwaysOnTop());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        };

        uploader.setProgressListener(listener);

        Video response = request.setAutoLevels(true)
                .setNotifySubscribers(true)
                .setStabilize(false)
                .setPrettyPrint(true)
                .execute();

        Main.out(response.toPrettyString());
        return response.getId();
    }
}
