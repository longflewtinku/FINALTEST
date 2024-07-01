package com.linkly.launcher.service;

import static com.linkly.libpositive.PosIntegrate.unpackConnectConfig;
import static com.linkly.libpositive.messages.IMessages.APP_CFG_SCHEDULE_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_DOWNLOAD_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_SUSPEND_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_REC_PERFORMED_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_BRANDING_FILES_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_UPDATE_COMPLETE_EVENT;
import static com.linkly.libpositive.messages.IMessages.BOOT_COMPLETED_EVENT;
import static com.linkly.libpositive.messages.IMessages.POS_NAV_BAR_EVENT;
import static com.linkly.libpositive.messages.IMessages.SERVICE_START_EVENT;
import static com.linkly.libpositive.messages.IMessages.TRANSACTION_RESULT_EVENT;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.linkly.launcher.BrandingConfig;
import com.linkly.launcher.LauncherApplication;
import com.linkly.launcher.work.UnattendedRebootWorker;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveScheduledEvent;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libpositive.wrappers.PositiveConnectConfig;
import com.linkly.libpositive.wrappers.PositiveConnectStatus;
import com.linkly.libpositivesvc.POSitiveSvcCheck;
import com.linkly.libpositivesvc.POSitiveSvcLib;
import com.linkly.libpositivesvc.downloader.DownloadDirector;
import com.linkly.libui.IUI;
import com.linkly.libui.UI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import timber.log.Timber;

public class LauncherController extends BroadcastReceiver {

    private static final String TAG = "LauncherController";
    private static ArrayList<PositiveEvent> eventList;
    public static ArrayList<PositiveScheduledEvent> scheduledEventList;

    private static boolean isRunning = false;

    static private IUI framework;

    public static void startPOSitiveSvc(Context context) {
        //Init Mal
        MalFactory.getInstance().initialiseMal(context);
        framework = UI.getInstance();
        framework = framework.initialiseUI(null);

        scheduledEventList = new ArrayList<>();


        Intent i = new Intent(context, Launcher.class);

        context.startService(i);

        //Set the flag to running
        isRunning = true;

    }

    public static ArrayList<PositiveScheduledEvent> getScheduledEventList() {
        return LauncherController.scheduledEventList;
    }

    public static IUI getFramework() {
        return LauncherController.framework;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.i("Received an Event: " + intent.getAction());

        if (!isRunning) {
            startPOSitiveSvc(context);
        }

        if (BOOT_COMPLETED_EVENT.equals(intent.getAction())) {
            //Messages.getInstance().sendAutoStartRequest(context);
            LauncherApplication.setCheckAutoAppStart(true);
        } else if (SERVICE_START_EVENT.equals(intent.getAction())) {
            Messages.getInstance().sendAutoStartRequest(context);
        } else if (TRANSACTION_RESULT_EVENT.equals(intent.getAction())) {
            // Not sure what this is doing in here. commented out as causing the launcher's activity to be displayed.
//            // TODO: difference between PositiveLib and POSIntergrade
        } else if (APP_DOWNLOAD_EVENT.equals(intent.getAction())) {
            boolean isForced = intent.getBooleanExtra("isForce", false);
            if (isForced) {
                DownloadDirector.forceUpdate(context);

            } else {
                DownloadDirector.update(context, false);
            }


        } else if (APP_UPDATE_COMPLETE_EVENT.equals(intent.getAction())) {

            if (framework == null) {
                MalFactory.getInstance().initialiseMal(context);
                framework = UI.getInstance().initialiseUI(null);
            }

            if (MalFactory.getInstance() != null) {
                UnattendedRebootWorker.rebootNow(context);
            }
        } else if (APP_CFG_SCHEDULE_EVENT.equals(intent.getAction())) {
            Timber.e("MalConfig Schedule Event");
            PositiveScheduledEvent event = POSitiveSvcLib.unpackScheduledEventConfig(intent);
            event.debugEvent();
            /*If cancel the remove, if Update, Remove the current, the Add the Updated one as if created*/
            if (event.getType() == PositiveScheduledEvent.EventType.UPDATE || event.getType() == PositiveScheduledEvent.EventType.CANCEL) {
                int i;
                for (i = 0; i < scheduledEventList.size(); i++) {
                    if (scheduledEventList.get(i).getAction().compareTo(event.getAction()) == 0) {
                        scheduledEventList.remove(i);
                        if (event.getType() == PositiveScheduledEvent.EventType.CANCEL) {
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }

            if (event.getType() == PositiveScheduledEvent.EventType.CREATE || event.getType() == PositiveScheduledEvent.EventType.UPDATE) {
                /*Add To Scheduled Event List With Soonest to front */
                if (scheduledEventList.size() == 0) {
                    scheduledEventList.add(event);
                } else {
                    int i;
                    for (i = 0; i < scheduledEventList.size(); i++) {
                        if (scheduledEventList.get(i).getTriggerTime() > event.getTriggerTime()) {
                            scheduledEventList.add(i, event);
                            return;
                        }
                    }
                    scheduledEventList.add(event);
                }

            }
        } else if (APP_REC_PERFORMED_EVENT.equals(intent.getAction())) {
            Timber.i("Rec Performed ");
            POSitiveSvcCheck.EFTSetLastRecDoneToday(context);
        } else if (APP_REBOOT_SUSPEND_EVENT.equals(intent.getAction())) {
            /*We Have received a Block Event for */
            MalFactory.getInstance().getHardware().setBlockSafeReboot(true);
        } else if(POS_NAV_BAR_EVENT.equals(intent.getAction())){
            Timber.d("Special event from non-linkly app to toggle back button");
            // special event from non-linkly app
            if( intent.hasExtra( "backEnabled" ) ){
                boolean backButtonEnabled = "1".equals( intent.getStringExtra( "backEnabled" ) );
                Timber.d("BackButton is to be = " + backButtonEnabled);
                if( backButtonEnabled ) {
                    DisplayKiosk.getInstance().onResume( true );
                    MalFactory.getInstance().getHardware().showBackButton();
                } else {
                    DisplayKiosk.getInstance().onResume( false );
                    MalFactory.getInstance().getHardware().hideBackButton();
                }
            }
        } else if (APP_SEND_EFT_BRANDING_FILES_EVENT.equals(intent.getAction())) {
            try {
                String dirBranding = MalFactory.getInstance().getFile().getCommonDir() + "/brandingFiles";
                Timber.i("Copy branding files over received in broadcast: " + dirBranding);
                MalFactory.getInstance().initialiseFiles(context);
                if (!(new File(dirBranding)).exists()) {
                    boolean ignored = (new File(dirBranding)).mkdirs();
                }
                copyResources(context, intent, dirBranding, false);
                BrandingConfig.getInstance().reloadBrandingData();
            } catch (Exception e) {
                Timber.w(e);
                Timber.i("APP_SEND_EFT_BRANDING_FILES_EVENT: Error receiving files from someone");
            }
        } else if ("com.linkly.APP_STATUS_INFO_REQUEST_EVENT".equals(intent.getAction())) {
            // Cannot infer POS connection from this event, need the config to be sure.
            Messages.getInstance().sendConnectConfigRequest(context);
        } else if ("com.linkly.CONNECT_CONFIG".equals(intent.getAction())) {
            PositiveConnectConfig connectConfig = unpackConnectConfig(context, intent);
            if (connectConfig == null) {
                Timber.e("connectConfig was null!");
                return;
            }
            LauncherApplication app = ((LauncherApplication) context.getApplicationContext());
            if (app.isPOSConnected() != null && app.isPOSConnected()
                    && !connectConfig.getConnectionStatus().equals(PositiveConnectStatus.CONNECTED)) {
                // POS connection lost
                Timber.e("...received CONNECT_CONFIG indicating POS is NOT connected!");
                app.onPOSConnectionLost();
            } else if (connectConfig.getConnectionStatus().equals(PositiveConnectStatus.CONNECTED)) {
                Timber.d("...received CONNECT_CONFIG indicating POS is connected...");
                app.setPOSConnected(
                        connectConfig.getConnectionStatus().equals(PositiveConnectStatus.CONNECTED));
            }
        }
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
                    Timber.i( "Invalid Channel");
                } else {
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    bResult = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                sourceChannel.close();
                destChannel.close();
            }
        } catch (Exception ex) {
            bResult = false;
        }

        return bResult;
    }
}
