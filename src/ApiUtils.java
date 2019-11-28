import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collection;

class ApiUtils {

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException If the client secrets file could not be found, could not be read, or another IO error occured.
     */
    private static Credential authorize(final NetHttpTransport httpTransport, JsonFactory JSON_FACTORY, Collection<String> SCOPES) throws IOException {
        String CLIENT_SECRETS = Main.LIBRARY_FOLDER + "/bin/client_secrets.json";
        // Load client secrets.
        FileInputStream in = new FileInputStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    static YouTube getService(JsonFactory JSON_FACTORY, Collection<String> SCOPES) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = ApiUtils.authorize(httpTransport, JSON_FACTORY, SCOPES);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("RedditVideoMaker")
                .build();
    }
}
