package main;

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
import java.util.Arrays;
import java.util.Collection;

class ApiUtils {
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload",
                    "https://www.googleapis.com/auth/youtube.force-ssl",
                    "https://www.googleapis.com/auth/youtube.readonly");
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

    private static YouTube service = null;

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException If the client secrets file could not be found, could not be read, or another IO error occured.
     */
    private static Credential authorize(Collection<String> scopes) throws IOException {
        String credentialDatastore = "rvm_yt_data";
        return authorize(scopes, credentialDatastore);
    }

    static Credential authorize(Collection<String> scopes, String credentialDatastore) throws IOException {
        String CLIENT_SECRETS = Config.getLibraryFolder() + "/bin/client_secrets.json";
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

        // Build the local server and bind it to port 8079 (8080+ is taken by the RVM server)
        LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8079).build();

        // Authorize.
        return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */
    static YouTube getService() throws GeneralSecurityException, IOException {
        if (service != null) {
            Main.out("YouTube service instance already exists! Returning previously-generated service.");
            return service;
        } else {
            Main.out("Generating new YouTube service...");
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = ApiUtils.authorize(SCOPES);
            service = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("RedditVideoMaker")
                    .build();
            return service;
        }
    }
}
