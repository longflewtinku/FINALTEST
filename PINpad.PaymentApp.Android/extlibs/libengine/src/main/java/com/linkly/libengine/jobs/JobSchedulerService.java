package com.linkly.libengine.jobs;

import static com.linkly.libpositive.messages.IMessages.APP_RESTART_EVENT;
import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.platform.StartupParams;
import com.linkly.libpositive.events.PositiveEvent;

import timber.log.Timber;

@TargetApi(21)
public class JobSchedulerService extends JobService {
    private static final String TAG = "JobSchedulerSvc";

    private Handler mJobHandler = new Handler( Looper.myLooper(), msg -> {
        JobParameters params = (JobParameters) msg.obj;
        PersistableBundle b = params.getExtras();

        String action = b.getString("action");
        if (BATCH_UPLOAD_EVENT.equals(action)) {
            Timber.i( "JobSchedulerService:Batch Upload Event Received");
            EFTJob job = new EFTJob(PositiveEvent.EventType.BATCH_UPLOAD);
            Engine.getJobs().add(job);
        } else if (APP_RESTART_EVENT.equals(action)) {

            Timber.i( "App Restart Event Received");
            Engine.getAppCallbacks().runApplication(getApplicationContext(), new StartupParams(false, false, false, false, false));
        }

        jobFinished((JobParameters) msg.obj, false);
        return true;
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        mJobHandler.sendMessage(Message.obtain(mJobHandler, 1, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }


}
