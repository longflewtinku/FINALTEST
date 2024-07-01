package com.linkly.payment.workmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.linkly.libengine.action.ReadHotloadParams;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class WorkManagerHotloadParams extends Worker {

    private static final String TAG = "WorkManagerHotloadParams";
    private static final int BACKOFF_TIMER = 10;

    public WorkManagerHotloadParams(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static OneTimeWorkRequest getWorkRequest() {
        return new OneTimeWorkRequest.Builder(WorkManagerHotloadParams.class)
                .addTag(TAG)
                .setInitialDelay(BACKOFF_TIMER, TimeUnit.SECONDS)
                .build();
    }

    public static void StartWorkRequest(Context context) {
        Timber.e("Starting Work Request");

        WorkManager.getInstance(context).enqueueUniqueWork(
                TAG,
                ExistingWorkPolicy.REPLACE,
                getWorkRequest()
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        // We don't know when the file could be updated (could be in boot/startup); check if the engine is initialised otherwise we will crash :(
        if(Engine.isIsInitialised() && !WorkflowScheduler.getInstance().isTerminalBusy(false)) {
            Timber.e("Queuing Workflow for ReadHotloadParams");
            WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new ReadHotloadParams()), false, false, false);
        } else {
            // Workflows can backoff but could end up waiting a while.
            // Faster for us to "start a new onetime work request" as do want to use increase the recheck time.
            Timber.e("Re-adding Work Request");
            StartWorkRequest(getApplicationContext());
        }

        //.OK.... So we return success regardless we handle the retires internally since we need to be aggressive in retrying.
        return Result.success();
    }
}
