package main;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ThumbnailSetResponse;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SetThumbnail {

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static void main(String videoId, String filePath)
            throws GeneralSecurityException, IOException {
        if (!Main.DEBUGGING) {

            YouTube youtubeService = ApiUtils.getService();
            File mediaFile = new File(filePath);
            InputStreamContent mediaContent =
                    new InputStreamContent("image/png",
                            new BufferedInputStream(new FileInputStream(mediaFile)));
            mediaContent.setLength(mediaFile.length());

            // Define and execute the API request
            YouTube.Thumbnails.Set request = youtubeService.thumbnails()
                    .set(videoId, mediaContent);

            MediaHttpUploader uploader = request.getMediaHttpUploader();

            long startTime = System.currentTimeMillis();

            MediaHttpUploaderProgressListener listener = progress -> SwingUtilities.invokeLater(() -> {
                try {
                    Main.out("Thumbnail upload progress changed to " + (progress.getProgress() * 100) + "%");
                    Main.setProgressValue((long) progress.getProgress() * 100);
                    Main.setMaxProgressValue(100);
                    Main.calculateRemainingTime(startTime, 1000, (long) progress.getProgress() * 1000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            uploader.setProgressListener(listener);

            ThumbnailSetResponse response = request.execute();
            System.out.println(response);
        } else {
            System.out.println("[WARNING] Thumbnail upload was skipped because `Main.DEBUGGING` is set to `true`.");
        }
    }
}
