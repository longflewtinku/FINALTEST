package com.linkly.payment.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class AutoSettlementBroadcastReceiver extends BroadcastReceiver {
    // The only intent supported is Alarm execution. No extras expected
    // Log entries have the "Error" level for initial testing on "release" version. Can be removed or changed to "information" after testing done

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.e("AutoSettlement: AutoSettlementBroadcastReceiver onReceive called");
        AutoSettlementWatcher.executeAlarm(context.getApplicationContext());
    }
}