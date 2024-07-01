package com.linkly.launcher.service;

import static com.linkly.launcher.BuildConfig.DEBUG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linkly.libmal.MalFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class Rebooter extends BroadcastReceiver {
    private final List<String> mParameterizedPackages = new ArrayList<>(Arrays.asList(
            "com.linkly.payment",
            "com.linkly.connect"
    ));

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!DEBUG) {
            Timber.d("onReceive[Rebooter]...intent: %s", intent.toUri(0));
            if (intent.getData().getSchemeSpecificPart().startsWith("com.linkly.")
                    && !mParameterizedPackages.contains(intent.getData().getSchemeSpecificPart())) {
                performReboot(context);
            }
        }
    }

    private void performReboot(Context context) {
        Timber.d("performReboot...");
        Timber.e("Performing reboot due to non-parameterized suite package replacement!");
        if (MalFactory.getInstance().getHardware() == null) {
            MalFactory.getInstance().initialiseMal(context);
        }
        MalFactory.getInstance().getHardware().reboot();
    }
}