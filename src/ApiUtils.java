import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collection;

class ApiUtils {
    /**
     * Define a global instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /**
     * This is the directory that will be used under the user's home directory where OAuth tokens will be stored.
     */
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException If the client secrets file could not be found, could not be read, or another IO error occured.
     */
    private static Credential authorize(Collection<String> scopes) throws IOException {
        String CLIENT_SECRETS = Config.getLibraryFolder() + "/bin/client_secrets.json";
        String credentialDatastore = "rvm_yt_data";
        // Load client secrets.
        FileInputStream in = new FileInputStream(CLIENT_SECRETS);
        Reader clientSecretsReader = new InputStreamReader(in);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretsReader);

        // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
                .build();

        // Build the local server and bind it to port 8080
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    static YouTube getService(Collection<String> SCOPES) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = ApiUtils.authorize(SCOPES);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("RedditVideoMaker")
                .build();
    }
}
