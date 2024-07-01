/*
 * *
 *     * ********************************************************************************
 *     * COPYRIGHT
 *     *               PAX TECHNOLOGY, Inc. PROPRIETARY INFORMATION
 *     *   This software is supplied under the terms of a license agreement or
 *     *   nondisclosure agreement with PAX  Technology, Inc. and may not be copied
 *     *   or disclosed except in accordance with the terms in that agreement.
 *     *
 *     *      Copyright (C) 2017 PAX Technology, Inc. All rights reserved.
 *     * ********************************************************************************
 *
 */

package com.linkly.libpositivesvc.paxstore;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.pm.PackageInfoCompat;

import com.linkly.libconfig.OverrideParameters;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.XmlParse;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositivesvc.utils.Utils;
import com.pax.market.android.app.sdk.StoreSdk;
import com.pax.market.api.sdk.java.base.constant.ResultCode;
import com.pax.market.api.sdk.java.base.dto.DownloadResultObject;
import com.pax.market.api.sdk.java.base.exception.NotInitException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import timber.log.Timber;

/**
 * Created by zcy on 2016/12/2 0002.
 */
public class DownloadParamService extends Service {

    private static final int NOTIFICATION_ID = 12345;

    public DownloadParamService() {
        // Service...
    }

    /**
     * Checks if there are resources ready to load into working directories
     *
     * @return
     */
    public static boolean isNewResourcesAvailable() {
        boolean isAvailable = false;

        IMalFile file = MalFactory.getInstance().getFile();
        File dir = new File(file.getCommonDir() + "/paxstore");

        if (dir.isDirectory()) {
            File[] dlist = dir.listFiles();

            if (dlist != null && dlist.length > 0) {
                isAvailable = true;
            }
        }


        return isAvailable;
    }

    private static final List<String> noRebootRequiredFiles = Arrays.asList("hotloadparams.xml");

    public static boolean requiresReboot(IMalFile root, File[] fileList, List<String> filesToIgnore) {
        // At this point only 1 file, but allows to extend. Also easier to filter on files we don't want to reboot.
        // Saves us comparing files that we don't need to check.
        List<File> filteredList = Arrays.stream(fileList)
                .filter(file -> !filesToIgnore.contains(file.getName()))
                .collect(Collectors.toList());

        return filteredList.stream().anyMatch(
                newFile -> {
                    // for some reason XML files are stored in a different path so we need to check against the XML config.
                    String toFilePath = (newFile.getName().contains(".xml") ? root.getWorkingDir() : root.getCommonDir()) + "/" + newFile.getName();
                    File currentFile = new File(toFilePath);
                    boolean notEqual = !Utils.filesContentEqual(currentFile, newFile);
                    Timber.e("Files %s Equal: %b", currentFile.getName(), notEqual);
                    // Doing a binary compare. If they're equal
                    return notEqual;
                }
        );
    }

    public static File[] getNewFiles(IMalFile file) {
        File dir = new File(file.getCommonDir() + "/paxstore");
        return dir.listFiles();
    }

    public static boolean loadNewResources() {
        boolean newResLoaded = false;

        if (isNewResourcesAvailable()) {
            IMalFile file = MalFactory.getInstance().getFile();
            File[] downloadedFiles = getNewFiles(file);

            for (File f : downloadedFiles) {
                /*Copy File into Common Dir */
                Timber.e( "Copying file : " + file.getCommonDir() + "/" + f.getName() + " to " + f.getPath());

                /* xml files need to go in a specific Path */
                if (f.getName().contains(".xml")) {
                    if (file.copyFile(f.getPath(), file.getWorkingDir() + "/" + f.getName())) { // files
                        file.deleteFile(f.getPath());
                    }
                } else {
                    if (file.copyFile(f.getPath(), file.getCommonDir() + "/" + f.getName())) { // EFT
                        file.deleteFile(f.getPath());
                    }
                }
            }
            newResLoaded = true;
        }


        return newResLoaded;
    }

    @SuppressWarnings("deprecation")
    private int getAppVersionCode() {

        if (MalFactory.getInstance() != null && MalFactory.getInstance().getHardware() != null) {
            return MalFactory.getInstance().getHardware().getAppVersionCode();
        }
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            return ( int ) PackageInfoCompat.getLongVersionCode( pInfo );
        } catch (PackageManager.NameNotFoundException e) {
            Timber.w(e);
        }
        return 1;
    }

    private void checkMal() {
        if (MalFactory.getInstance().getHardware() == null) {
            MalFactory.getInstance().initialiseMal(this);
        }
    }

    @SuppressWarnings("deprecation")
    public static String getInstalledPackagePreciseName(String packageName, PackageManager packageManager) {
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {

            if (packageInfo.packageName.contains(packageName))
                return packageInfo.packageName;
        }
        return null;
    }
    @SuppressWarnings("deprecation")

    public static int checkForCoreApps(PackageManager packageManager) {

        int coreApps = 0;
        if (packageManager == null)
            return 0;

        List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);

        if (packages == null || packages.size() <= 0)
            return 0;

        for (ApplicationInfo packageInfo : packages) {

            if (packageInfo.packageName.compareToIgnoreCase("com.linkly.secapp") == 0)
                coreApps++;

            if (packageInfo.packageName.contains("com.linkly.res"))
                coreApps++;

            if (packageInfo.packageName.compareToIgnoreCase("com.linkly.launcher") == 0)
                coreApps++;

            if (packageInfo.packageName.compareToIgnoreCase("com.linkly.payment") == 0)
                coreApps++;

        }

        Timber.e("CoreApps installed:" + coreApps);
        return coreApps;
    }


    /*New API Implementation */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Specifies the download path for the parameter file, you can replace the path to your app's internal storage for security.
        String saveFilePath = MalFactory.getInstance().getFile().getCommonDir() + "/paxstore/";

        Thread thread = new Thread(() -> {
            //Call SDK API to download parameter files into your specific directory,
            DownloadResultObject downloadResult = null;
            try {
                int versionCode = getAppVersionCode();
                downloadResult = StoreSdk.getInstance().paramApi().downloadParamToPath(getApplication().getPackageName(), versionCode, saveFilePath);
                Timber.e(downloadResult.toString());
            } catch (NotInitException e) {
                Timber.e(e);
            }

            if (downloadResult != null && downloadResult.getBusinessCode() == ResultCode.SUCCESS.ordinal()) {
                //todo can start to add your logic.
                Timber.e( "Pax Store Download");

                if (isNewResourcesAvailable()) {
                    Timber.e("New Resources Available");

                    // Load resources always overwrites the current files, have to do the compare BEFORE we call load new resources.
                    boolean rebootRequired = requiresReboot(MalFactory.getInstance().getFile(), getNewFiles(MalFactory.getInstance().getFile()), noRebootRequiredFiles);
                    if (loadNewResources() && (rebootRequired)) {
                        checkMal();
                        XmlParse parse = new XmlParse();
                        OverrideParameters overrideParams = parse.parse("overrideparams.xml", OverrideParameters.class);

                        checkLanguage(overrideParams.getLanguage());
                        if (getInstalledPackagePreciseName("com.linkly.eftlauncher", getPackageManager()) != null) {
                            Timber.e("Don't reboot as eftlauncher running");
                        } else {
                            while (true) {
                                int count = checkForCoreApps(getPackageManager());
                                if (count >= 4) {
                                    Util.Sleep(1000);
                                    Timber.e("Force Reboot");
                                    MalFactory.getInstance().getHardware().reboot();
                                }
                                Util.Sleep(2000);
                            }
                        }
                    }
                }
            } else {
                //update download fail info in main page for Demo
                Timber.e("ErrorCode: " + downloadResult.getBusinessCode() + "ErrorMessage: " + downloadResult.getMessage());
            }
        });
        thread.start();
        return START_STICKY;
    }

    public static boolean checkLanguage(String language) {
        // get the current Locale
        // if language has changed we set it on the ISys interface
        // return true if language has changed and we need to reboot
        // DO NOT SET if not valid language

        if (Util.isNullOrEmpty(language))
            return false;

        String currentLocale;
        try {

            if (MalFactory.getInstance() != null) {
                currentLocale = MalFactory.getInstance().getHardware().getSystemLanguage();

                if (currentLocale != null && currentLocale.compareToIgnoreCase(language) != 0 && language.split("_").length == 2) {

                    Locale locale = new Locale(language.split("_")[0], language.split("_")[1]);
                    if (locale != null) {
                        int iRet = MalFactory.getInstance().getHardware().setSystemLanguage(locale);
                        if (iRet == 0) {
                            return true;
                        }
                    }
                    Timber.i( "setSystemLanguage failed:" + language);
                }

            }
        } catch (Exception ex) {
            Timber.w(ex);
            return false;
        }
        return false;


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // do stuff like register for BroadcastReceiver, etc.
                // Create the Foreground Service
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
                Notification notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(android.R.color.transparent)
                        .setPriority(PRIORITY_MIN)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .build();

                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Timber.e( e.getMessage());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "100001";
        String channelName = "Download Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}

