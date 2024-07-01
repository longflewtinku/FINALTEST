package com.linkly.payment.application;

import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_BRANDING_FILES_EVENT;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.global.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/***
 * Handles processing and delivering of Branding files and colours to other apps.
 * Always required to be called on start up as we don't know if new apps that require
 * it have been added.
 */
public class BrandingProcessor {
    private void makeBrandingDirectory(String dirBranding) {
        File brandingDir = new File(dirBranding);

        if (!brandingDir.exists()) {
            boolean successfullyCreated = brandingDir.mkdirs();
            Timber.d("Created %s - %b", dirBranding, successfullyCreated);
        }
    }

    /***
     * Processes and sends our branding to other applications.
     * Our config only ever comes into the payment app.
     * @param payCfg our current payment app config
     * @param mal required for file stuff
     * @param context required for activity sending etc.
     */
    public void processAndSendBranding(PayCfg payCfg, IMal mal, Context context) {

        // copy over Branding parameters to another apps
        String jsonBranding = "";
        try {
            JSONObject json = getJsonObject(payCfg);
            jsonBranding = json.toString();
        } catch (Exception e) {
            Timber.i("Processing Branding parameters error");
            Timber.w(e);
        }

        // copy brand files to subfolder for easier distribution across apps
        String dirBranding = mal.getFile().getCommonDir() + "/brandingFiles";

        makeBrandingDirectory(dirBranding);

        processBrandFile(payCfg.getBrandDisplayLogoHeader(), mal.getFile());
        processBrandFile(payCfg.getBrandDisplayLogoIdle(), mal.getFile());
        processBrandFile(payCfg.getBrandDisplayLogoSplash(), mal.getFile());
        processBrandFile(payCfg.getBrandReceiptLogoHeader(), mal.getFile());

        // Create Branding parameters file
        File destination = new File(dirBranding + "/brandingParameters.json");
        try(Writer output = new BufferedWriter(new FileWriter(destination))) {
            output.write(jsonBranding);
        } catch (Exception ex) {
            Timber.w(ex);
        }

        if (context != null) {
            sendFilesToActivity(APP_SEND_EFT_BRANDING_FILES_EVENT, context, context.getPackageName(),
                    dirBranding, "com.linkly.connect.linkly", "com.linkly.connect.service.LauncherReceiver");
            sendFilesToActivity(APP_SEND_EFT_BRANDING_FILES_EVENT, context, context.getPackageName(),
                    dirBranding, "com.linkly.launcher", "com.linkly.launcher.service.LauncherController");
        }
    }

    @NonNull
    private static JSONObject getJsonObject(PayCfg payCfg) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("brandDisplayLogoHeader", payCfg.getBrandDisplayLogoHeader());
        json.put("brandDisplayLogoIdle", payCfg.getBrandDisplayLogoIdle());
        json.put("brandDisplayLogoSplash", payCfg.getBrandDisplayLogoSplash());
        json.put("brandDisplayStatusBarColour", payCfg.getBrandDisplayStatusBarColour());
        json.put("brandDisplayButtonColour", payCfg.getBrandDisplayButtonColour());
        json.put("brandDisplayButtonTextColour", payCfg.getBrandDisplayButtonTextColour());
        json.put("brandDisplayPrimaryColour", payCfg.getBrandDisplayPrimaryColour());
        json.put("brandReceiptLogoHeader", payCfg.getBrandReceiptLogoHeader());
        return json;
    }

    private void processBrandFile(String brandFilename, IMalFile file) {
        if (!Util.isNullOrEmpty(brandFilename)) {
            String srcFile = file.getCommonDir() + "/" + brandFilename;
            if (file.fileExist(srcFile)) {
                file.copyFile(srcFile, file.getCommonDir() + "/brandingFiles" + "/" + brandFilename);
            }
        }
    }
    @SuppressWarnings("deprecation")
    public boolean sendFilesToActivity(String action, Context context, String fromPackage, String fromDirectory, String destinationPackage, String destinationDirectory) {
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

        File instDir = new File(fromDirectory);
        File[] filesToInstall = instDir.listFiles();

        ArrayList<Uri> uris = new ArrayList<>();

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> activities = packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // Loop the activity list adding permissions
        int size = activities.size();

        if (filesToInstall != null) {

            for (int i = 0; i < size; i++) {
                ResolveInfo resolveInfo = activities.get(i);
                for (File f : filesToInstall) {

                    Uri uri = FileProvider.getUriForFile(context, fromPackage + ".provider", f);
                    context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.getApplicationContext().grantUriPermission(fromPackage, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                    context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.getApplicationContext().grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }
            }

            for (File f : filesToInstall) {

                Uri uri = FileProvider.getUriForFile(context, fromPackage + ".provider", f);
                uris.add(uri);
            }
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

        Timber.i("Send Branding files to: " + destinationPackage + " : " + destinationDirectory);

        context.sendBroadcast(intent);

        return false;
    }

}
