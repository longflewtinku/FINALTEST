package com.linkly.launcher.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.linkly.launcher.BuildConfig;
import com.linkly.libmal.MalFactory;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/*
There should not be multiple instances of this Worker scheduled at any given time, so this class
goes to effort to ensure only one instance of this Worker is ever scheduled.

Also any reboot performed outside of this class must cancel this Worker to avoid a dangling
scheduled reboot! To mitigate that, all reboots in the app should be performed through this class'
rebootNow method.
 */
public class UnattendedRebootWorker extends Worker {
    private static final String WORKER_TAG = "UnattendedRebootWorker";
    private Context mContext;

    public static void scheduleReboot(Context context) {
        int rebootWorkTimeoutMins = 30;
        if (BuildConfig.DEBUG) {
            rebootWorkTimeoutMins = 5;
        }
        OneTimeWorkRequest rebootWork = new OneTimeWorkRequest.Builder(UnattendedRebootWorker.class)
                .addTag(WORKER_TAG)
                .setInitialDelay(rebootWorkTimeoutMins, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueue(rebootWork);
    }

    public static Result rebootNow(Context context) {
        cancelSelf(context);
        try {
            MalFactory.getInstance().getHardware().reboot();
        } catch (Exception e) {
            return Result.failure();
        }
        return Result.success();
    }

    public static void cancelSelf(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORKER_TAG);
    }

    public UnattendedRebootWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
        cancelSelf(mContext);
    }

    @Override
    public Result doWork() {
        Timber.e("doWork...Unattended Mode Forced Reboot!");
        return rebootNow(mContext);
    }
}