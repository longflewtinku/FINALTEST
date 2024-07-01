package com.linkly.libpositivesvc.downloader;

import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_WARNING_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_SVC_PROGRESS_DIALOG;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.Messages;

import timber.log.Timber;

/**
 * I have created this class as a abstraction between TMS Technologies
 *
 *
 */



public class DownloadDirector {
    private static boolean inProgress = false;
    public static boolean isSilent = false;
    private static boolean lastRequestSuccess = false;

    public static boolean isInProgress() {
        return DownloadDirector.inProgress;
    }

    public static boolean isLastRequestSuccess() {
        return DownloadDirector.lastRequestSuccess;
    }

    public enum tmsSystem{PaxStoreTMS};
    public static tmsSystem systemInUse = tmsSystem.PaxStoreTMS;



    public static void newProgressDialog(String title, String message, boolean intermediate) {

        try {
            if (isSilent)
                return;

            Intent intent = new Intent();
            intent.setAction(APP_SVC_PROGRESS_DIALOG);
            intent.putExtra("PROGRESS", true);
            intent.putExtra("TITLE", title);
            intent.putExtra("MESSAGE", message);
            LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(intent);
        }catch (Exception ex){
            Timber.w(ex);
        }
    }

    public static void dismissProgressDialog() {

        try {
            if (isSilent)
                return;

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent intent = new Intent();
                    intent.setAction(APP_SVC_PROGRESS_DIALOG);
                    intent.putExtra("PROGRESSDISMISS", true);
                    LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(intent);
                }
            }, 1500);
        }catch (Exception ex){
            Timber.w(ex);
        }
    }

    public static void update(Context context, boolean silent){
        isSilent = silent;
        switch (systemInUse){
            case PaxStoreTMS:
                Messages.getInstance().sendPaxStoreUpdateRequest(context);
                break;
        }
    }


    public static void forceUpdate(Context context){

        isSilent = false;
        switch (systemInUse){
            case PaxStoreTMS:
                Messages.getInstance().sendPaxStoreUpdateRequest(context);
                break;
        }
    }

    public static void updateComplete(boolean success, boolean reboot) {
        dismissProgressDialog();
        if(reboot){
            MalFactory.getInstance().getHardware().SafeReboot(MalFactory.getInstance().getMalContext(), 3,  APP_REBOOT_WARNING_EVENT);
        }

        lastRequestSuccess = success;
        inProgress = false;
        isSilent = false;
    }



    public static void activateDevice(Context context, String storeId, String deptId, String licence, String storekey){
        isSilent = false;
        inProgress = true;
    }


    public static void deactivateDevice(Context context){
        isSilent = false;
    }
}
