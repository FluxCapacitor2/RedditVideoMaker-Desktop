import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ThumbnailSetResponse;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;

public class SetThumbnail {
    private static final Collection<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/youtube.force-ssl");

    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Call function to create API service object. Define and
     * execute API request. Print API response.
     *
     * @throws GeneralSecurityException, IOException, GoogleJsonResponseException
     */
    public static void main(String videoId, String filePath)
            throws GeneralSecurityException, IOException {

        YouTube youtubeService = ApiUtils.getService(SCOPES);
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
                Main.gui.progressLabel.setText(Math.round(progress.getProgress() * 100) + "%");
                Main.calculateRemainingTime(startTime, 100, (long) (progress.getProgress() * 100));
                Main.gui.progressBar.setValue((int) (progress.getProgress() * 100));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        uploader.setProgressListener(listener);

        ThumbnailSetResponse response = request.execute();
        System.out.println(response);
    }
}
