package de.androidcrypto.googledriveplayground;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UploadInternalActivity extends AppCompatActivity {

    private static final String TAG = "UploadInternalActivity";

    /**
     * This activity will upload a file from internal storage to Google Drive
     * Make sure that the files was created using "Generate files"
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_internal);

        // this activity only checks for 1 file in internal storage
        String filename = "txtfile1.txt";
        boolean fileExists = checkForInternalStorageFile(filename);
        Log.i(TAG, "the filename " + filename + " is existing: " + fileExists);
        Toast.makeText(UploadInternalActivity.this, "the file " + filename + " is existing: " + fileExists, Toast.LENGTH_SHORT).show();
        if (!fileExists) {
            finish();
        }

        try {
            createFolder();
            System.out.println("*** folder created");
        } catch (IOException e) {
            System.out.println("### folder NOT created");
            //throw new RuntimeException(e);
        }


        try {
            uploadBasic(filename);
            Toast.makeText(UploadInternalActivity.this, "upload success for " + filename, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(UploadInternalActivity.this, "upload ERROR for " + filename, Toast.LENGTH_SHORT).show();
            //throw new RuntimeException(e);
        }



    }

    private boolean checkForInternalStorageFile(String filename) {
        java.io.File file = getBaseContext().getFileStreamPath(filename);
        return file.exists();
    }


    /**
     * Upload new file.
     *
     * @return Inserted file metadata if successful, {@code null} otherwise.
     * @throws IOException if service account credentials file not found.
     *
     * source: https://github.com/googleworkspace/java-samples/blob/main/drive/snippets/drive_v3/src/main/java/UploadBasic.java
     */
    public String uploadBasic(String filename) throws IOException {
        Log.i(TAG, "uploadBasic");
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.
        /*
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));

         */

        System.out.println("*** generate credentials");

        /*
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Arrays.asList(DriveScopes.DRIVE));
         */
        final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
        final String CREDENTIALS_FILE_PATH = "/credentials.json";
        final String TOKENS_DIRECTORY_PATH = "tokens";
        final List<String> SCOPES =
                Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

        InputStream in = UploadInternalActivity.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        NetHttpTransport HTTP_TRANSPORT = null;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            //throw new RuntimeException(e);
            System.out.println("#### ERROR: " + e.getMessage());
        }

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("GoogleDrivePlayground")
                .build();
/*
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singletonList(DriveScopes.DRIVE));

        System.out.println("*** requestInitializer");

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        System.out.println("*** build a new authorized API client service");

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("GoogleDrivePlayground")
                .build();
*/
        System.out.println("*** get file from filename");

        // Upload file photo.jpg on drive.
        File fileMetadata = new File();
        //fileMetadata.setName("photo.jpg");
        fileMetadata.setName(filename);
        // File's content.
        // java.io.File filePath = new java.io.File("files/photo.jpg");
        java.io.File filePath = new java.io.File(getFilesDir(), filename);

        boolean fileExist = filePath.exists();
        System.out.println("*** fileExist: " + fileExist + " for " + filePath.getAbsolutePath());

        // Specify media type and file-path for file.
        //FileContent mediaContent = new FileContent("image/jpeg", filePath);
        FileContent mediaContent = new FileContent("text/plain", filePath);

        try {
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("*** File ID: " + file.getId());
            Log.i(TAG, "the file " + filename + " was uploaded with FileId: " + file.getId());
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            Log.e(TAG, "ERROR: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Create new folder.
     *
     * @return Inserted folder id if successful, {@code null} otherwise.
     * @throws IOException if service account credentials file not found.
     */
    public String createFolder() throws IOException {
        // Load pre-authorized user credentials from the environment.
        // TODO(developer) - See https://developers.google.com/identity for
        // guides on implementing OAuth2 for your application.
        /*
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Arrays.asList(DriveScopes.DRIVE_FILE));
        */

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        // Build a new authorized API client service.
        Drive service = new Drive.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Drive samples")
                .build();
        // File's metadata.
        File fileMetadata = new File();
        fileMetadata.setName("Test");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("Folder ID: " + file.getId());
            return file.getId();
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to create folder: " + e.getDetails());
            throw e;
        }
    }
}