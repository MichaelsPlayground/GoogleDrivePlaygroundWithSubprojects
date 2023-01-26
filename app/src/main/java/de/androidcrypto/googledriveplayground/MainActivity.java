package de.androidcrypto.googledriveplayground;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import de.androidcrypto.googledriveplayground.ammarptn.MainActivity4;
import de.androidcrypto.googledriveplayground.dbtest.ui.MainActivity3;
import de.androidcrypto.googledriveplayground.prateekbangre.MainActivity2;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "GD Playground Main";

    private final String basicFilename = "txtfile";
    private final String fileExtension = ".txt";

    Button subGoogleDriveDemo;
    Button subDatabaseTest;
    Button subGDriveRest;
    Button subBaMusic;
    Button generateFiles, signIn, queryFiles;
    Button uploadFileFromInternalStorage;
    com.google.android.material.textfield.TextInputEditText fileName;


    private DriveServiceHelper mDriveServiceHelper;
    private static final int REQUEST_CODE_SIGN_IN = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateFiles = findViewById(R.id.btnMainGenerateFiles);
        signIn = findViewById(R.id.btnMainSignIn);
        queryFiles = findViewById(R.id.btnMainQueryFiles);
        fileName = findViewById(R.id.etMainFilename);

        uploadFileFromInternalStorage = findViewById(R.id.btnMainUploadFile);

        subGoogleDriveDemo = findViewById(R.id.btnMainSubGoogleDriveDemo);
        subDatabaseTest = findViewById(R.id.btnMainSubDbTest);
        subGDriveRest = findViewById(R.id.btnMainSubGDriveRest);
        subBaMusic = findViewById(R.id.btnMainSubBAMusic);

        subBaMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "start sub project BAMusic");
                Intent intent = new Intent(MainActivity.this, de.androidcrypto.googledriveplayground.bamusic.MainActivity.class);
                startActivity(intent);
            }
        });

        uploadFileFromInternalStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "upload a file from internal storage");
                Intent intent = new Intent(MainActivity.this, UploadInternalActivity.class);
                startActivity(intent);
            }
        });

        subGDriveRest.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Log.i(TAG, "start sub project GDriveRest");
        Intent intent = new Intent(MainActivity.this, MainActivity4.class);
        startActivity(intent);
    }
});


        subGoogleDriveDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "start sub project GoogleDriveDemo");
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
                // finish();
            }
        });

        subDatabaseTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "start sub project Database Test");
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                startActivity(intent);
                // finish();
            }
        });

        queryFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "query files on Google Drive");

                if (mDriveServiceHelper == null) {
                    Log.e(TAG, "ERROR on Querying for files");
                    return;
                }

                    mDriveServiceHelper.queryFiles()
                            .addOnSuccessListener(fileList -> {
                                System.out.println("received filelist");
                                System.out.println("list entries: " + fileList.getFiles().size());
                                StringBuilder builder = new StringBuilder();
                                for (File file : fileList.getFiles()) {
                                    //builder.append(file.getName()).append("\n");
                                    builder.append("fileName: " + file.getName() + " fileId:" + file.getId()).append("\n");
                                    System.out.println("fileName: " + file.getName()
                                            + " fileId: " + file.getId()
                                    + " fileSize: " + file.getSize());

                                }
                                String fileNames = builder.toString();
                                System.out.println(fileNames);
                                fileName.setText(fileNames);
                                //mDocContentEditText.setText(fileNames);

                                //etReadOnlyMode();
                            })
                            .addOnFailureListener(exception -> Log.e(TAG, "Unable to query files.", exception));
            }
        });

        generateFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "generate files");
                // this is generating 5 text files in internal storage
                String basicString = "This is a test file for uploading to Google Drive.\nIt is file number ";

                int numberOfFiles = 5;
                for (int i = 1; i < numberOfFiles + 1; i++) {
                    FileWriter writer = null;
                    try {
                        String filename = basicFilename + i + fileExtension;
                        String dataToWrite = basicString + i + "\n" +
                                "generated on " + new Date();
                        java.io.File file = new java.io.File(view.getContext().getFilesDir(), filename);
                        writer = new FileWriter(file);
                        writer.append(dataToWrite);
                        writer.flush();
                        writer.close();
                        Log.i(TAG, "file generated number: " + i);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(MainActivity.this, "generated " + numberOfFiles + " files in internal storage", Toast.LENGTH_SHORT).show();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign in to Google Drive");
                // Authenticate the user. For most apps, this should be done when the user performs an
                // action that requires Drive access rather than in onCreate.
                requestSignIn();
            }
        });
    }

    /**
     * section sign-in to Google Drive account
     */

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        // DriveScopes.DRIVE shows ALL files
        // DriveScopes.DRIVE_FILE shows only files uploaded by this app
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();
/*
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
*/
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        // todo handle deprecated startActivityForResult
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.i(TAG, "Signed in as " + googleAccount.getEmail());
                    Toast.makeText(MainActivity.this, "Signed in as " + googleAccount.getEmail(), Toast.LENGTH_SHORT).show();

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("GoogleDrivePlayground")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Unable to sign in.", exception);
                    Toast.makeText(MainActivity.this, "Unable to sign in: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * section onActivityResult
     * todo handle deprecated startActivityForResult
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;
/*
            case REQUEST_CODE_OPEN_DOCUMENT:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFileFromFilePicker(uri);
                    }
                }
                break;
 */
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

}