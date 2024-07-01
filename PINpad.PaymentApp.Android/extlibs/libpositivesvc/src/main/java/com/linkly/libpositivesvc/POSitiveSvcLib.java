package com.linkly.libpositivesvc;

import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_FILES_EVENT;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveScheduledEvent;
import com.linkly.libpositive.messages.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Utility class used to communicate with the apay application to initiate transactions.
 */

public class POSitiveSvcLib {

    private static final String TAG = "com.linkly.launcher.lib";

    /* Retrieve the version number of this library */
    public static String getVersion() {
        return "0.0.2";
    }

    /**
     * Sends a configuration event for a Scheduled Task
     *
     * @param event
     */
    public static void configureScheduledEvent(Context context, PositiveScheduledEvent event) {
        if (context != null) {
            Messages.getInstance().sendScheduleEventRequest(context, event);
        }
    }

    public static PositiveScheduledEvent unpackScheduledEventConfig(Intent intent) {
        PositiveScheduledEvent event = new PositiveScheduledEvent(PositiveScheduledEvent.EventType.values()[intent.getIntExtra("type", 0)], intent.getStringExtra("action"), intent.getLongExtra("triggerTime", 0));
        return event;
    }

    @SuppressWarnings("deprecation")
    public static String isPackageInstalled(String packageName, PackageManager packageManager) {

        List<ApplicationInfo> packages = packageManager.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.contains(packageName))
                return packageInfo.packageName;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static long getPackageInstallDate(String packageName, PackageManager packageManager) {
        try {
            if (!Util.isNullOrEmpty(packageName)) {
                long installed = packageManager.getPackageInfo(packageName, 0).lastUpdateTime;
                Timber.i( Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", installed));
                return installed;
            }
        } catch ( Exception e) {
            Timber.w(e);
        }
        return 0;
    }

    public static boolean copyFile(FileInputStream src, String dst) {

        boolean bResult = false;
        if (src == null || dst == null) {
            return false;
        }
        File Dest = new File(dst);

        try {
            FileChannel sourceChannel = null;
            FileChannel destChannel = null;
            try {


                Timber.i( "Copy file to " + dst );
                sourceChannel = src.getChannel();
                destChannel = new FileOutputStream(Dest).getChannel();
                if (destChannel == null || sourceChannel == null) {
                    Timber.i("Invalid Channel");
                } else {
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    bResult = true;
                }
            } catch (Exception ex) {
                Timber.w(ex);
            } finally {
                sourceChannel.close();
                destChannel.close();
            }
        } catch (Exception ex) {
            bResult = false;
        }


        return bResult;
    }

    @SuppressWarnings("deprecation")
    public static void copyResources(Context context, Intent intent, String directoryName, boolean deleteOriginal) {

        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if (uris != null) {
            for (Uri u : uris) {
                File f = new File(u.getPath());
                ContentResolver contentResolver = context.getApplicationContext().getContentResolver();

                try {
                    MalFactory.getInstance().initialiseFiles(context);
                    FileInputStream src = new FileInputStream(contentResolver.openFileDescriptor(u, "r").getFileDescriptor());
                    Timber.i( "SRC:" + src + " DIR:" + directoryName + " FNAME:" + f.getName());
                    copyFile(src, directoryName + "/" + f.getName());
                    src.close();
                    if (deleteOriginal) {
                        context.getContentResolver().delete(u, null, null);
                    }
                } catch (FileNotFoundException e) {
                    Timber.w(e);
                } catch (IOException e) {
                    Timber.w(e);
                } catch (SecurityException e) {
                    Timber.w(e);
                }

            }
        }
    }

    @SuppressWarnings("deprecation")
    /* removes all resources that start with the same thing "com.linkly.res" will remove "com.linkly.resoptomany" */
    public static boolean removeResources(final Context context, final String packageName) {

        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(0);
            for (ApplicationInfo packageInfo : packages) {
                if (packageInfo.packageName.contains(packageName)) {
                    final String precisePackageName = isPackageInstalled(packageName, context.getPackageManager());
                    if (precisePackageName != null) {

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {

                                IMal imal = MalFactory.getInstance();
                                imal.initialiseMal(context);

                                if (!precisePackageName.isEmpty() && MalFactory.getInstance().getHardware().uninstallApp(precisePackageName)) {
                                    Timber.i("Uninstalled resource package after grabbing it's resources");
                                }

                            }
                        });
                        return true;
                    }
                }
            }

        } catch ( Exception e) {
            Timber.w(e);
        }
        return false;
    }

    public static void saveResInstallTime(final Context context, long lastInstallTime) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("resLastInstallTime", lastInstallTime);

        Timber.i("resLastInstallTime saved as :%s", Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", lastInstallTime));

        editor.commit();
    }

    @SuppressWarnings("deprecation")
    public static boolean checkResources(final Context context, final String packageName, final String responseActivity) {

        try {
            final String precisePackageName = isPackageInstalled(packageName, context.getPackageManager());
            if (precisePackageName != null) {

                final long installTime = getPackageInstallDate(precisePackageName, context.getPackageManager());
                long lastInstallTime = PreferenceManager.getDefaultSharedPreferences(context).getLong("resLastInstallTime", 0);
                Timber.i("LastInstallTime:%s v NewInstallTime:%s", Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", lastInstallTime), Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", installTime));

                if (installTime > lastInstallTime) {
                    saveResInstallTime(context, installTime);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Timber.i("Try to start resource install");
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(precisePackageName, "com.linkly.res.MainActivity"));
                                intent.putExtra("RESPONSE_ACTIVITY", responseActivity);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                Timber.e("Start resource install Failed on Exception");
                            }
                        }
                    });

                    return true;
                }
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }
        return false;
    }


    /* delete any resource packages and send the resources to the apps listening */
    public static boolean sendResources(Context context) {
        POSitiveSvcLib.sendFilesByBroadcast(APP_SEND_EFT_FILES_EVENT, context, "com.linkly.launcher", MalFactory.getInstance().getFile().getCommonDir(), "com.linkly.payment", "com.linkly.payment.positivesvc.MessageReceiver");
        POSitiveSvcLib.sendFilesByBroadcast(APP_SEND_EFT_FILES_EVENT, context, "com.linkly.launcher", MalFactory.getInstance().getFile().getCommonDir(), "com.linkly.secapp", "com.linkly.secapp.service.P2PEReceiver");
        POSitiveSvcLib.sendFilesByBroadcast(APP_SEND_EFT_FILES_EVENT, context, "com.linkly.launcher", MalFactory.getInstance().getFile().getCommonDir(), "com.linkly.connect.demo", "com.linkly.connect.service.LauncherReceiver");
        POSitiveSvcLib.sendFilesByBroadcast(APP_SEND_EFT_FILES_EVENT, context, "com.linkly.launcher", MalFactory.getInstance().getFile().getCommonDir(), "com.linkly.connect.linkly", "com.linkly.connect.service.LauncherReceiver");
        return true;
    }


    @SuppressWarnings("deprecation")
    public static boolean sendFilesByBroadcast(String action, Context context, String fromPackage, String fromDirectory, String destinationPackage, String destReceiver  ) {

        Intent intent = new Intent();

        intent.setAction(action);
        intent.setComponent(new ComponentName(destinationPackage, destReceiver));
        Timber.i( "SEND FILES from directory:" +fromDirectory + " to pkg: "+ destinationPackage + " to receiver: " + destReceiver);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String dir = fromDirectory;

        File instDir = new File(dir);
        File[] filesToInstall = instDir.listFiles();

        ArrayList<Uri> uris = new ArrayList<Uri>();

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> activities = packageManager.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // Loop the activity list adding permissions
        int size = activities.size();
        for(int i=0;i<size;i++)
        {
            ResolveInfo resolveInfo = activities.get(i);
            // Get activity package name.
            String packageName = resolveInfo.activityInfo.packageName;

            for (File f : filesToInstall) {

                if (!f.isDirectory()) {
                    String s = f.getPath();
                    Timber.i( "SEND FILES SOURCE:" + s + ":" + fromPackage);
                    Uri uri = FileProvider.getUriForFile(context, fromPackage, f);
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                }

            }
        }


        for (File f : filesToInstall) {

            if (!f.isDirectory()) {
                Uri uri = FileProvider.getUriForFile(context, fromPackage, f);
                uris.add(uri);
            }
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);

        Timber.i( "Send App Update Request");
        context.sendBroadcast(intent);
        return false;
    }


    /******************************************************************************************/
    public static void copyFileFromAssets(Context ctx, String srcFile, String destFile) throws IOException {
        Timber.i("prepare copy ASSERT/" + srcFile + "file to" + destFile);

        InputStream iptStm = null;
        OutputStream optStm = null;

        try {
            iptStm = ctx.getAssets().open(srcFile);
            Timber.i("AssetsFilePath:" + srcFile + " FileSize:" + (iptStm == null ? 0 : iptStm.available()));
            Timber.i("strDesFilePath:" + destFile);

            if (iptStm == null) {
                Timber.i("file[" + srcFile + "]not exists in the ASSERT,don't need to copy!");
                return;
            }

            File file = new File(destFile);
            if (!file.exists()) {// file not exists,need to copy
                if (!file.createNewFile()) {
                    return;
                }
                Runtime.getRuntime().exec("chmod 766 " + file);
            } else {
                if (file.length() == iptStm.available()) {
                    Timber.i("File is consistent, do not need to copy!");
                    iptStm.close();
                    return;
                }
                if (!file.delete() || !file.createNewFile()) {
                    return;
                }
                Runtime.getRuntime().exec("chmod 766 " + file);
            }

            optStm = new FileOutputStream(file);

            int nLen;

            byte[] buff = new byte[1024];
            while ((nLen = iptStm.read(buff)) > 0) {
                optStm.write(buff, 0, nLen);
            }
        } catch (Exception e) {
            Timber.i(e.getMessage());
        } finally {
            if (optStm != null) {
                optStm.close();
            }
            if (iptStm != null) {
                iptStm.close();
            }
        }
    }

}
