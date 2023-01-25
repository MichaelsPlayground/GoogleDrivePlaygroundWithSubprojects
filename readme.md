# Google Drive playground


build.gradle (app):
````plaintext

    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/ASL2.0'
    }


    // google drive libs
    //implementation 'com.google.android.gms:play-services-auth:17.0.0'
    implementation 'com.google.android.gms:play-services-auth:20.4.1'
    //implementation 'com.google.http-client:google-http-client-gson:1.26.0'
    implementation 'com.google.http-client:google-http-client-gson:1.41.8'
    implementation('com.google.api-client:google-api-client-android:1.26.0')
    implementation('com.google.apis:google-api-services-drive:v3-rev136-1.25.0')
    implementation 'com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava'
    
    // for subproject prateekbangre - GoogleDrive_demo
    // for runtime permission
    implementation 'com.karumi:dexter:6.2.1'
    //for top side toast, text with animation
    implementation 'net.steamcrafted:load-toast:1.0.12'    
````

AndroidManifest.xml:
```plaintext
<uses-permission android:name="android.permission.INTERNET" />

    <!-- Permissions required by Google Auth -->
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"/>
```

For DriveServiceHelper.java see here:

https://github.com/ammarptn/GDrive-Rest-Android/blob/master/gdriverest/src/main/java/com/ammarptn/gdriverest/DriveServiceHelper.java

https://stackoverflow.com/questions/56949872/can-someone-provide-an-up-to-date-android-guide-for-google-drive-rest-api-v3

https://stackoverflow.com/questions/69933562/android-deprecated-tasks-call-replacement

https://developers.google.com/android/reference/com/google/android/gms/tasks/Tasks

https://developers.google.com/android/reference/com/google/android/gms/tasks/TaskCompletionSource

https://myksb1223.github.io/develop_diary/2019/04/13/Google-Drive-api-in-Android.html


Subproject prateekbangre - GoogleDrive_demo

Source: https://github.com/prateekbangre/GoogleDrive_demo

**Don't forget to manually given permission to ALL FILES** using SETTINGS/APPS/GoogleDrivePlayground app

A downloaded files is in Emulator: /storage/emulated/0/Download 

The file path is set in prateekbangre/FilesAdapter.java/

private static final String STORAGE_FOLDER_PATH = "/storage/emulated/0/Download";

https://ammar.lanui.online/integrate-google-drive-rest-api-on-android-app-bc4ddbd90820

https://github.com/ammarptn/GDrive-Rest-Android

# Registering for Google Drive

As we discuss we need to Google Drive SDK, So in order to use it, we need to enable that API. Okay don't worry we will move forward step by step

- Go to [Google Console](https://console.developers.google.com/projectselector/apis/dashboard).
- Sign up for a developer account if We don't have or then sign in.
- `Create` a project Or `Select` and click continue from below,

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/9tkmgg6z7s5fup0vxfxs.png)

- After creating a project we have `Dashboard` like below,

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/yd30f54tv84u4zbbqwup.png)


- Now, Select `Library` on the left-hand side to go to the Search screen
- Type “Google Drive” and select Google Drive API.
- Select Enable. Then It looks like below,

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/3jyyhw1qc16zdii50mye.png)

- Back to Dashboard, from Dashboard left-hand side go to Credentials. Create `Credential` by selecting the `OAuth client ID` like below

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/985j2fol9qo84nd04gln.png)

Tt navigates to the `Create OAuth client ID` page. Select our Application Type `Android`.

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/uc5b0l55srkulmptlo6x.png)

- Now we need your `SHA-1` `Signing-certificate fingerprint` key. Do so follow below steps
- Copy the keytool text (press the Copy icon) and paste it into a terminal.
- Change the path-to-debug-or-production-keystore to your default debug keystore location:
- On Mac or Linux, `~/.android/debug.keystore` .
- On Windows, `%USERPROFILE%/.android/debug.keystore` .
- After you execute the command, you will be prompted to enter the keystore password. The password for the debug keystore is blank by default, so you can just press Return or Enter.


> Notes: If throwing error to generate singing key then it might be `keystore` not found.

I used an alternative command for my machine `MAC OS`.

```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```


If everything works correctly, you should see something like this:
- Copy the `SHA1` value from the terminal into the text field and press Create. The Client ID dialog will appear. Press OK.

![Alt Text](https://thepracticaldev.s3.amazonaws.com/i/yeb93rnpvwwfspb7u6wc.png)

- Finally, enter a name and the `package name` that we used to `create our app`. Although the hint refers to the package name in `AndroidManifest.xml`, it has to match the `applicationId` in `build.gradle` instead — otherwise, the login flow will fail.
- We don't need to complete the form that appears, it's optional so press `Save` and move on.

> Optional, Now, In the Credentials page. Authorization on Android uses the `SHA1` fingerprint and `package name` to `identify your app`, so you `don’t have to download` any `JSON file or copy any API key` or secret to our project.

It about lots man, Don't worry! Now, are in the fun part 😎.

