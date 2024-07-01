package com.linkly.res;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.FileProvider;

import com.linkly.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends Activity {

    private static final String APP_SEND_EFT_FILES_EVENT = "com.linkly.APP_SEND_EFT_FILES";
    private static final String CHANGE_ACCESS_PERMISSION_766 = "chmod 766 ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup our logging
        Logger.init(BuildConfig.DEBUG);
        installResources(this, getIntent());

        Handler loadingHandler = new Handler(Looper.getMainLooper());
        loadingHandler.postDelayed(MainActivity.this::finish, 100);
    }


    @SuppressWarnings("deprecation")
    public static boolean sendFilesToActivity(String action, Context context, String fromPackage, String fromDirectory, String destinationPackage, String destinationDirectory) {
        if (destinationDirectory == null)
            return true;

        Intent intent = new Intent();

        intent.setAction(action);
        intent.setComponent(new ComponentName(destinationPackage, destinationDirectory));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        String dir = fromDirectory;

        File instDir = new File(dir);
        File[] filesToInstall = instDir.listFiles();

        ArrayList<Uri> uris = new ArrayList<>();

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // Loop the activity list adding permissions
        int size = activities.size();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = activities.get(i);
            for (File f : filesToInstall) {
                Uri uri = FileProvider.getUriForFile(context, fromPackage, f);
                context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }
        }

        for (File f : filesToInstall) {
            Uri uri = FileProvider.getUriForFile(context, fromPackage, f);
            uris.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        Timber.i("Send App Update Request to: " + destinationPackage + " : " + destinationDirectory);
        context.startActivity(intent);
        return false;
    }

    public static void installResources(Context context, Intent intent) {
        AssetManager assets = context.getAssets();
        String tmpFolderPath = "/data/data/" + context.getPackageName() + "/files/EFT";
        File f = new File(tmpFolderPath);
        if (!f.exists())
            f.mkdir();

        try {
            Runtime.getRuntime().exec(CHANGE_ACCESS_PERMISSION_766 + f);

            String[] files = assets.list("");
            for (String file : files) {
                Timber.i("File Name: " + file);
                copyFileFromAssets(context, file, tmpFolderPath + "/" + file);
            }

        } catch (Exception ex) {
            Timber.w(ex);
        }

        String responseActivity = intent.getStringExtra("RESPONSE_ACTIVITY");

        if (Build.MODEL.compareTo("E500") == 0 || Build.MODEL.compareTo("E600") == 0 || Build.MODEL.compareTo("E700") == 0 || Build.MODEL.compareTo("E800") == 0) {
            sendFilesToActivity(APP_SEND_EFT_FILES_EVENT, context, context.getPackageName(), tmpFolderPath, "com.linkly.payment", responseActivity);
        } else {
            sendFilesToActivity(APP_SEND_EFT_FILES_EVENT, context, context.getPackageName(), tmpFolderPath, "com.linkly.launcher", responseActivity);
        }
    }


    /******************************************************************************************/
    @SuppressWarnings("java:S4042") // Using the already tested approach. Sonar recommendation to use Java.nio.file package is available only from API level 26
    private static void copyFileFromAssets(Context ctx, String srcFile, String destFile) throws IOException {
        Timber.i("prepare copy ASSERT/" + srcFile + " file to" + destFile);

        try (InputStream iptStm = ctx.getAssets().open(srcFile)) {

            Timber.i("AssetsFilePath:" + srcFile + " FileSize:" + iptStm.available());
            Timber.i("strDesFilePath:" + destFile);

            File file = new File(destFile);
            if (!file.exists()) {// file not exists,need to copy
                if (!file.createNewFile()) {
                    return;
                }
                Runtime.getRuntime().exec(CHANGE_ACCESS_PERMISSION_766 + file);
            } else {
                if (file.length() == iptStm.available()) {
                    Timber.i("File is consistent, do not need to copy!");
                    return;
                }
                if (!file.delete() || !file.createNewFile()) {
                    return;
                }
                Runtime.getRuntime().exec(CHANGE_ACCESS_PERMISSION_766 + file);
            }

            writeDataFromAsset(iptStm, file);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private static void writeDataFromAsset(InputStream iptStm, File file) {
        try (OutputStream optStm = new FileOutputStream(file)) {
            byte[] buff = new byte[1024];
            int nLen;
            while ((nLen = iptStm.read(buff)) > 0) {
                optStm.write(buff, 0, nLen);
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }
    }
}