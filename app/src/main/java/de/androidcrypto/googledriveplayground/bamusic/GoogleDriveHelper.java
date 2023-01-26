package de.androidcrypto.googledriveplayground.bamusic;


import static de.androidcrypto.googledriveplayground.bamusic.MainActivity.userAccount;

import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.util.Collections;

public class GoogleDriveHelper {

    private static final String TAG = "GoogleDriveHelper";

    private Drive googleDriveService;

    GoogleDriveHelper(Context context)  {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                //Collections.singleton(DriveScopes.DRIVE_FILE)
                Collections.singleton(DriveScopes.DRIVE)
        );
        if (userAccount != null) {
            System.out.println("*** GoogleDriveHelper init ***");
            credential.setSelectedAccount(userAccount.getAccount());
            googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential
            ).setApplicationName("BAMusic").build();
        } else {
            Log.d(TAG, "userAccount is null");
        }
        try {
            System.out.println("*** googleDriveService: " + googleDriveService.drives().list());
        } catch (IOException e) {
            System.out.println("*** googleDriveService: NO LIST");
        }
    }
}
