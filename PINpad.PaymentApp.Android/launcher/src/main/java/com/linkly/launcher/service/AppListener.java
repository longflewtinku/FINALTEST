package com.linkly.launcher.service;

import static com.linkly.libpositive.wrappers.OperatingMode.BACKGROUND_CAPABLE;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.linkly.libpositive.messages.Messages;

import timber.log.Timber;

public class AppListener extends BroadcastReceiver {

    private static final String LINKLY_CONNECT_APP_PKG_NAME = "com.linkly.connect.linkly";
    private static final String MAIN_SERVICE = "com.linkly.connect.service.MainService";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getData() != null && intent.getData().getEncodedSchemeSpecificPart().equals(LINKLY_CONNECT_APP_PKG_NAME)) {
            Timber.i("Start Linkly Connect Services");

            Messages.getInstance().sendOperatingMode(context, BACKGROUND_CAPABLE.ordinal());
            ComponentName componentName = new ComponentName(LINKLY_CONNECT_APP_PKG_NAME, MAIN_SERVICE);
            Intent startConnectAppIntent = new Intent();
            startConnectAppIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startConnectAppIntent.setComponent(componentName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(startConnectAppIntent);
            } else {
                context.startService(startConnectAppIntent);
            }
        }
    }
}
