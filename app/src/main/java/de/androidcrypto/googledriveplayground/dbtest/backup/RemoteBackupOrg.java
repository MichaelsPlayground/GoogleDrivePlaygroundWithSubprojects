/*
 *   Copyright 2016 Marco Gomiero
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package de.androidcrypto.googledriveplayground.dbtest.backup;

import static de.androidcrypto.googledriveplayground.dbtest.db.DBHelper.DATABASE_NAME;
import static de.androidcrypto.googledriveplayground.dbtest.ui.MainActivity3.REQUEST_CODE_CREATION;
import static de.androidcrypto.googledriveplayground.dbtest.ui.MainActivity3.REQUEST_CODE_OPENING;
import static de.androidcrypto.googledriveplayground.dbtest.ui.MainActivity3.REQUEST_CODE_SIGN_IN;

import android.content.IntentSender;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.androidcrypto.googledriveplayground.dbtest.ui.MainActivity3;

public class RemoteBackupOrg {

    private static final String TAG = "Google Drive Activity";

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    public TaskCompletionSource<DriveId> mOpenItemTaskSource;

    private MainActivity3 activity;

    public RemoteBackupOrg(MainActivity3 activity) {
        Log.i(TAG, "init RemoteBackup");
        this.activity = activity;
    }

    public void connectToDrive(boolean backup) {
        Log.i(TAG, "connect to drive, isBackup: " + backup);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            Log.i(TAG, "account == null, sign in");
            signIn();
        } else {
            Log.i(TAG, "user is signed in, init the drive api");
            //Initialize the drive api
            mDriveClient = Drive.getDriveClient(activity, account);
            // Build a drive resource client.
            mDriveResourceClient = Drive.getDriveResourceClient(activity, account);
            if (backup) {
                Log.i(TAG, "startDriveBackup");
                startDriveBackup();
            }
            else {
                Log.i(TAG, "startDriveRestore");
                startDriveRestore();
            }

        }
    }

    private void signIn() {
        Log.i(TAG, "Start sign in");
        GoogleSignInClient GoogleSignInClient = buildGoogleSignInClient();
        activity.startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        /* org
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
*/
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();

        return GoogleSignIn.getClient(activity, signInOptions);
    }


    private void startDriveBackup() {
        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task -> createFileIntentSender(task.getResult()))
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new contents.", e));
    }

    private Task<Void> createFileIntentSender(DriveContents driveContents) {

        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            OutputStream outputStream = driveContents.getOutputStream();

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle("database_backup.db")
                .setMimeType("application/db")
                .build();


        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATION, null, 0, 0, 0);
                            return null;
                        });
    }


    private void startDriveRestore() {
        pickFile()
                .addOnSuccessListener(activity,
                        driveId -> retrieveContents(driveId.asDriveFile()))
                .addOnFailureListener(activity, e -> {
                    Log.e(TAG, "No file selected", e);
                });
    }

    private void retrieveContents(DriveFile file) {

        //DB Path
        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        Task<DriveContents> openFileTask = mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);

        openFileTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                        // Open the empty db as the output stream
                        OutputStream output = new FileOutputStream(inFileName);

                        // Transfer bytes from the inputfile to the outputfile
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }

                        // Close the streams
                        output.flush();
                        output.close();
                        fileInputStream.close();
                        Toast.makeText(activity, "Import completed", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                    }
                    return mDriveResourceClient.discardContents(contents);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Unable to read contents", e);
                    Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                });
    }

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        mDriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith((Continuation<IntentSender, Void>) task -> {
                    activity.startIntentSenderForResult(
                            task.getResult(), REQUEST_CODE_OPENING, null, 0, 0, 0);
                    return null;
                });
        return mOpenItemTaskSource.getTask();
    }

    private Task<DriveId> pickFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/db"))
                        .setActivityTitle("Select DB File")
                        .build();
        return pickItem(openOptions);
    }


}